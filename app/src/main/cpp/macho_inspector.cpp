#include "macho_inspector.h"
#include <fstream>
#include <sstream>
#include <vector>
#include <cstring>

namespace dreamcompat {
static uint32_t read32(const uint8_t* p, bool little) {
    if (little) return uint32_t(p[0]) | (uint32_t(p[1])<<8) | (uint32_t(p[2])<<16) | (uint32_t(p[3])<<24);
    return (uint32_t(p[0])<<24) | (uint32_t(p[1])<<16) | (uint32_t(p[2])<<8) | uint32_t(p[3]);
}
static bool valid_range(uint64_t off, uint64_t len, size_t total) {
    return off <= total && len <= static_cast<uint64_t>(total) - off;
}
static std::string cpu_name(uint32_t cpu) {
    switch(cpu) {
        case 0x0100000c: return "ARM64";
        case 0x01000007: return "x86_64";
        case 12: return "ARM";
        case 7: return "x86";
        default: return "unknown";
    }
}
MachOInfo inspect_macho_header(const uint8_t* b, size_t n) {
    MachOInfo o;
    if (!b || n < 4) { o.summary="Header too short."; return o; }
    uint32_t magic=read32(b,false);
    if (magic==0xcafebabe || magic==0xcafebabf || magic==0xbebafeca || magic==0xbfbafeca) {
        o.valid=true; o.is_fat=true; o.summary="Universal/FAT Mach-O detected; full slice parsing pending."; return o;
    }
    bool le64=b[0]==0xcf&&b[1]==0xfa&&b[2]==0xed&&b[3]==0xfe;
    bool be64=b[0]==0xfe&&b[1]==0xed&&b[2]==0xfa&&b[3]==0xcf;
    bool le32=b[0]==0xce&&b[1]==0xfa&&b[2]==0xed&&b[3]==0xfe;
    bool be32=b[0]==0xfe&&b[1]==0xed&&b[2]==0xfa&&b[3]==0xce;
    if (!(le64||be64||le32||be32)||n<16) { o.summary="Not a recognized thin Mach-O header."; return o; }
    bool little=le64||le32;
    o.valid=true; o.is_64_bit=le64||be64;
    o.cpu_type=read32(b+4,little); o.file_type=read32(b+12,little);
    o.architecture=cpu_name(o.cpu_type);
    o.summary=std::string("Mach-O ")+(o.is_64_bit?"64-bit":"32-bit")+"; architecture: "+o.architecture+". Android requires ELF, not Mach-O.";
    return o;
}
std::string inspect_macho_file(const std::string& path) {
    std::ifstream in(path,std::ios::binary);
    if(!in) return "Cannot open copied binary.";
    in.seekg(0,std::ios::end); std::streamoff end=in.tellg();
    if(end<32 || end>static_cast<std::streamoff>(1024LL*1024LL*1024LL)) return "Invalid file size or exceeds 1 GiB inspection limit.";
    size_t size=static_cast<size_t>(end); in.seekg(0,std::ios::beg);
    std::vector<uint8_t> data(size);
    if(!in.read(reinterpret_cast<char*>(data.data()),static_cast<std::streamsize>(size))) return "Could not read complete file.";
    MachOInfo info=inspect_macho_header(data.data(),data.size());
    if(!info.valid) return info.summary;
    if(info.is_fat) return info.summary;
    if(!info.is_64_bit) return info.summary+"\n32-bit load command parsing is not enabled.";
    bool little=data[0]==0xcf;
    uint32_t ncmds=read32(data.data()+16,little);
    uint32_t commandBytes=read32(data.data()+20,little);
    if(ncmds>100000 || !valid_range(32,commandBytes,size)) return info.summary+"\nInvalid load command table.";
    std::vector<std::string> libraries;
    std::string platform="not specified";
    uint64_t cursor=32;
    for(uint32_t i=0;i<ncmds;i++) {
        if(!valid_range(cursor,8,size)) return info.summary+"\nTruncated load command.";
        const uint8_t* cmd=data.data()+cursor;
        uint32_t kind=read32(cmd,little), bytes=read32(cmd+4,little);
        if(bytes<8 || !valid_range(cursor,bytes,size)) return info.summary+"\nInvalid load command size.";
        uint32_t base=kind & 0x7fffffffU;
        if((base==0xc || base==0x18 || base==0x1f || base==0x20 || base==0x23) && bytes>=24) {
            uint32_t nameOffset=read32(cmd+8,little);
            if(nameOffset<bytes) {
                const char* name=reinterpret_cast<const char*>(cmd+nameOffset);
                size_t max=bytes-nameOffset, len=strnlen(name,max);
                libraries.emplace_back(name,len);
            }
        }
        if(kind==0x32 && bytes>=24) {
            uint32_t p=read32(cmd+8,little);
            if(p==1) platform="macOS";
            else if(p==2) platform="iOS";
            else if(p==6) platform="Mac Catalyst";
            else if(p==7) platform="iOS Simulator";
            else platform="platform id "+std::to_string(p);
        } else if(kind==0x24) platform="macOS (legacy deployment command)";
        cursor+=bytes;
    }
    std::ostringstream out;
    out<<info.summary<<"\nMach-O file type: "<<info.file_type<<"\nTarget platform: "<<platform<<"\nLoad commands: "<<ncmds<<"\nLinked libraries/frameworks: "<<libraries.size();
    for(const auto& lib:libraries) out<<"\n  "<<lib;
    out<<"\n\nStatic inspection only: no code was loaded or executed.";
    return out.str();
}
}

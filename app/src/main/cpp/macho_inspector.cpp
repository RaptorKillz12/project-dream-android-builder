#include "macho_inspector.h"
namespace dreamcompat {
static uint32_t read_le32(const uint8_t* p) { return uint32_t(p[0]) | (uint32_t(p[1])<<8) | (uint32_t(p[2])<<16) | (uint32_t(p[3])<<24); }
MachOInfo inspect_macho_header(const uint8_t* b, size_t n) {
 MachOInfo o; if (!b || n<4) { o.summary="Header too short."; return o; }
 uint32_t be=(uint32_t(b[0])<<24)|(uint32_t(b[1])<<16)|(uint32_t(b[2])<<8)|b[3];
 if (be==0xcafebabe || be==0xcafebabf || be==0xbebafeca || be==0xbfbafeca) { o.valid=true; o.is_fat=true; o.summary="Universal/FAT Mach-O detected; slice inspection is pending."; return o; }
 bool le64=b[0]==0xcf&&b[1]==0xfa&&b[2]==0xed&&b[3]==0xfe; bool be64=b[0]==0xfe&&b[1]==0xed&&b[2]==0xfa&&b[3]==0xcf;
 bool le32=b[0]==0xce&&b[1]==0xfa&&b[2]==0xed&&b[3]==0xfe; bool be32=b[0]==0xfe&&b[1]==0xed&&b[2]==0xfa&&b[3]==0xce;
 if (!(le64||be64||le32||be32)||n<16) { o.summary="Not a recognized thin Mach-O header."; return o; }
 o.valid=true; o.is_64_bit=le64||be64; o.cpu_type=(le64||le32)?read_le32(b+4):(uint32_t(b[4])<<24)|(uint32_t(b[5])<<16)|(uint32_t(b[6])<<8)|b[7];
 switch(o.cpu_type) { case 0x0100000c:o.architecture="ARM64";break; case 0x01000007:o.architecture="x86_64";break; case 12:o.architecture="ARM";break; case 7:o.architecture="x86";break; default:o.architecture="unknown CPU type"; }
 o.summary=std::string("Mach-O ")+(o.is_64_bit?"64-bit":"32-bit")+"; architecture: "+o.architecture+". Android requires ELF; this binary cannot be loaded directly."; return o;
}
}

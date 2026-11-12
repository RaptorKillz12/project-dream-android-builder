#pragma once
#include <cstdint>
#include <cstddef>
#include <string>
namespace dreamcompat {
struct MachOInfo { bool valid=false; bool is_64_bit=false; bool is_fat=false; uint32_t cpu_type=0; uint32_t file_type=0; std::string architecture; std::string summary; };
MachOInfo inspect_macho_header(const uint8_t* bytes, size_t length);
}

extends RefCounted
class_name DreamMachOInspector

const MH_MAGIC_64 := 0xFEEDFACF
const MH_CIGAM_64 := 0xCFFAEDFE
const LC_LOAD_DYLIB := 0x0C
const LC_LOAD_WEAK_DYLIB := 0x80000018
const LC_REEXPORT_DYLIB := 0x8000001F
const LC_BUILD_VERSION := 0x32

static func inspect_file(path: String) -> Dictionary:
	var result := {"path": path, "valid": false, "format": "Unknown", "architecture": "Unknown", "file_type": "Unknown", "platform": "Unknown", "dependencies": [], "error": ""}
	var file := FileAccess.open(path, FileAccess.READ)
	if file == null:
		result.error = "Cannot open file: " + error_string(FileAccess.get_open_error())
		return result
	if file.get_length() < 32:
		result.error = "File is too small for a 64-bit Mach-O header."
		file.close()
		return result
	var magic := file.get_32()
	if magic != MH_MAGIC_64 and magic != MH_CIGAM_64:
		result.error = "Not a supported thin 64-bit little-endian Mach-O file."
		file.close()
		return result
	var cpu := file.get_32()
	var subtype := file.get_32()
	var filetype := file.get_32()
	var command_count := file.get_32()
	var command_bytes := file.get_32()
	file.get_32()
	file.get_32()
	result.valid = true
	result.format = "Mach-O 64-bit"
	result.architecture = _cpu_name(cpu, subtype)
	result.file_type = _file_type_name(filetype)
	var command_start := 32
	var command_end := mini(command_start + command_bytes, int(file.get_length()))
	for index in range(mini(command_count, 65536)):
		if file.get_position() + 8 > command_end:
			break
		var command := file.get_32()
		var size := file.get_32()
		if size < 8 or file.get_position() - 8 + size > command_end:
			result.error = "Malformed load command at index %d." % index
			break
		var command_base := file.get_position() - 8
		if command == LC_LOAD_DYLIB or command == LC_LOAD_WEAK_DYLIB or command == LC_REEXPORT_DYLIB:
			var name_offset := file.get_32()
			if name_offset >= 8 and name_offset < size:
				file.seek(command_base + name_offset)
				var name_bytes := file.get_buffer(size - name_offset)
				var zero := name_bytes.find(0)
				if zero >= 0:
					name_bytes = name_bytes.slice(0, zero)
				result.dependencies.append(name_bytes.get_string_from_utf8())
		elif command == LC_BUILD_VERSION and size >= 24:
			var platform := file.get_32()
			result.platform = _platform_name(platform)
		file.seek(command_base + size)
	file.close()
	return result

static func _cpu_name(cpu: int, subtype: int) -> String:
	match cpu:
		0x0100000C:
			return "ARM64"
		0x01000007:
			return "x86_64"
		12:
			return "ARM"
		7:
			return "x86"
		_:
			return "Unknown CPU 0x%08X (subtype 0x%08X)" % [cpu, subtype]

static func _file_type_name(value: int) -> String:
	match value:
		1: return "Object"
		2: return "Executable"
		6: return "Dynamic Library"
		8: return "Bundle"
		_: return "Mach-O file type %d" % value

static func _platform_name(value: int) -> String:
	match value:
		1: return "macOS"
		2: return "iOS"
		6: return "Mac Catalyst"
		7: return "iOS Simulator"
		_: return "Platform %d" % value

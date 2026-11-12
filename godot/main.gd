extends Control

const STEPS := ["Welcome", "Game files", "Verification", "Components", "Configuration", "Build"]
const ACCENT := Color("#56A9F5")
const MUTED := Color("#A9B9CC")
const MachOInspector = preload("res://macho_inspector.gd")
var page := 0
var source_path := ""
var detected: Dictionary = {}
var component_checks := {"Runtime compatibility": true, "macOS / POSIX API compatibility": true, "Graphics translation layer": true, "IL2CPP support layer": true, "MelonLoader integration": false, "GamePatches integration": false, "Mod support": false}
var renderer := "OpenGL ES 3"
var log_text := ""
var body: VBoxContainer
var step_label: Label
var back_button: Button
var next_button: Button
var source_edit: LineEdit
var file_dialog: FileDialog
var log_view: RichTextLabel

func _ready() -> void:
	_build_shell()
	_draw_page()

func _build_shell() -> void:
	var bg := ColorRect.new()
	bg.color = Color("#0D1522")
	bg.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(bg)
	var root := VBoxContainer.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	root.add_theme_constant_override("separation", 0)
	add_child(root)
	var header := PanelContainer.new()
	header.add_theme_stylebox_override("panel", _style(Color("#172338"), 0))
	root.add_child(header)
	var head := HBoxContainer.new()
	header.add_child(head)
	var logo := Label.new()
	logo.text = "◈"
	logo.add_theme_font_size_override("font_size", 30)
	logo.add_theme_color_override("font_color", ACCENT)
	head.add_child(logo)
	var title_col := VBoxContainer.new()
	head.add_child(title_col)
	var title := Label.new()
	title.text = "PROJECT DREAM SETUP"
	title.add_theme_font_size_override("font_size", 20)
	title.add_theme_color_override("font_color", Color.WHITE)
	title_col.add_child(title)
	var subtitle := Label.new()
	subtitle.text = "ANDROID EDITION  •  SETUP WIZARD"
	subtitle.add_theme_font_size_override("font_size", 10)
	subtitle.add_theme_color_override("font_color", MUTED)
	title_col.add_child(subtitle)
	var progress_panel := PanelContainer.new()
	progress_panel.add_theme_stylebox_override("panel", _style(Color("#111D2D"), 0))
	root.add_child(progress_panel)
	var progress := HBoxContainer.new()
	progress.add_theme_constant_override("separation", 5)
	progress_panel.add_child(progress)
	for i in range(STEPS.size()):
		var segment := ColorRect.new()
		segment.name = "Progress%d" % i
		segment.custom_minimum_size = Vector2(0, 5)
		segment.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		segment.color = ACCENT if i == 0 else Color("#34445A")
		progress.add_child(segment)
	var scroll := ScrollContainer.new()
	scroll.size_flags_vertical = Control.SIZE_EXPAND_FILL
	root.add_child(scroll)
	body = VBoxContainer.new()
	body.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	body.add_theme_constant_override("separation", 12)
	scroll.add_child(body)
	var footer := PanelContainer.new()
	footer.add_theme_stylebox_override("panel", _style(Color("#172338"), 0))
	root.add_child(footer)
	var foot := HBoxContainer.new()
	footer.add_child(foot)
	step_label = Label.new()
	step_label.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	step_label.add_theme_color_override("font_color", MUTED)
	foot.add_child(step_label)
	back_button = Button.new()
	back_button.text = "Back"
	back_button.pressed.connect(_back)
	foot.add_child(back_button)
	next_button = Button.new()
	next_button.text = "Next"
	next_button.pressed.connect(_next)
	foot.add_child(next_button)
	file_dialog = FileDialog.new()
	file_dialog.file_mode = FileDialog.FILE_MODE_OPEN_DIR
	file_dialog.access = FileDialog.ACCESS_FILESYSTEM
	file_dialog.dir_selected.connect(_on_directory_selected)
	add_child(file_dialog)

func _draw_page() -> void:
	for child in body.get_children():
		child.queue_free()
	var heading := Label.new()
	heading.add_theme_font_size_override("font_size", 25)
	heading.add_theme_color_override("font_color", Color.WHITE)
	body.add_child(heading)
	var desc := Label.new()
	desc.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	desc.add_theme_color_override("font_color", MUTED)
	body.add_child(desc)
	match page:
		0:
			heading.text = "Welcome to Project Dream"
			desc.text = "This setup wizard checks an original Unity game folder and prepares an Android compatibility build configuration."
			_add_card("SETUP WIZARD", "Select the original game, verify its files, choose components and configure the Android target.")
			_add_notice("A real macOS-to-Android execution runtime is still required before the original Mac game can run.")
		1:
			heading.text = "Select your game files"
			desc.text = "Choose the extracted SonicDreamTeam.app/Contents directory."
			var row := HBoxContainer.new()
			body.add_child(row)
			source_edit = LineEdit.new()
			source_edit.size_flags_horizontal = Control.SIZE_EXPAND_FILL
			source_edit.placeholder_text = "Game Contents folder path"
			source_edit.text = source_path
			row.add_child(source_edit)
			var browse := Button.new()
			browse.text = "Browse"
			browse.pressed.connect(func(): file_dialog.popup_centered_ratio(0.85))
			row.add_child(browse)
			_add_notice("Android folder access depends on the device file picker and storage permissions. Choose an accessible extracted Contents folder.")
		2:
			heading.text = "Verify game files"
			desc.text = "Run basic file-presence checks. This does not validate binary compatibility."
			var verify := Button.new()
			verify.text = "Run verification"
			verify.pressed.connect(_verify_source)
			body.add_child(verify)
			if detected.is_empty():
				_add_notice("No verification has been run yet.")
			for key in detected:
				_add_status_row(str(key), str(detected[key]))
		3:
			heading.text = "Choose setup components"
			desc.text = "Choose which components to request in the build configuration."
			for key in component_checks:
				var cb := CheckBox.new()
				cb.text = key
				cb.button_pressed = component_checks[key]
				cb.toggled.connect(func(value: bool): component_checks[key] = value)
				body.add_child(cb)
			_add_notice("Selections are configuration only; they do not indicate that these runtime components are implemented.")
		4:
			heading.text = "Configure Android build"
			desc.text = "Select the graphics backend and review the target."
			var label := Label.new()
			label.text = "Graphics backend"
			body.add_child(label)
			var options := OptionButton.new()
			options.add_item("OpenGL ES 3")
			options.add_item("Vulkan (planned)")
			options.item_selected.connect(func(i: int): renderer = "OpenGL ES 3" if i == 0 else "Vulkan")
			body.add_child(options)
			_add_status_row("Target", "Android ARM64")
			_add_status_row("Output", "Build plan only — not a playable game APK")
			_add_status_row("Renderer", renderer)
		5:
			heading.text = "Build configuration"
			desc.text = "Generate a portable JSON build plan from the selected options."
			var build := Button.new()
			build.text = "Generate build plan"
			build.pressed.connect(_generate_plan)
			body.add_child(build)
			log_view = RichTextLabel.new()
			log_view.fit_content = true
			log_view.custom_minimum_size.y = 120
			log_view.text = log_text if log_text != "" else "Build log will appear here."
			body.add_child(log_view)
			_add_notice("This milestone does not translate or execute the macOS Mach-O game binary.")
	step_label.text = "Step %d of %d  •  %s" % [page + 1, STEPS.size(), STEPS[page]]
	back_button.disabled = page == 0
	next_button.text = "Finish" if page == STEPS.size() - 1 else "Next"
	var progress := get_child(1).get_child(1).get_child(0)
	for i in range(STEPS.size()):
		progress.get_child(i).color = ACCENT if i <= page else Color("#34445A")

func _next() -> void:
	if page == 1:
		source_path = source_edit.text.strip_edges() if source_edit else source_path
		if source_path.is_empty():
			_set_log("Select a game Contents folder before continuing.")
			return
	if page == 2 and detected.is_empty():
		_verify_source()
		return
	if page < STEPS.size() - 1:
		page += 1
		_draw_page()
	else:
		_set_log("Wizard finished. Generate the build plan on this page.")

func _back() -> void:
	if page > 0:
		if page == 1 and source_edit:
			source_path = source_edit.text.strip_edges()
		page -= 1
		_draw_page()

func _on_directory_selected(path: String) -> void:
	source_path = path
	_draw_page()

func _verify_source() -> void:
	if source_edit:
		source_path = source_edit.text.strip_edges()
	detected.clear()
	if source_path.is_empty():
		detected["Folder"] = "Missing — select a folder first"
	else:
		var assembly_path := _find_file_path(source_path, "GameAssembly.dylib")
		var metadata_path := _find_file_path(source_path, "global-metadata.dat")
		var plist_path := _find_file_path(source_path, "Info.plist")
		detected["GameAssembly.dylib"] = assembly_path if assembly_path != "" else "Not found"
		detected["global-metadata.dat"] = metadata_path if metadata_path != "" else "Not found"
		detected["Info.plist"] = plist_path if plist_path != "" else "Not found"
		detected["Source"] = source_path
		if assembly_path != "":
			var binary_report: Dictionary = MachOInspector.inspect_file(assembly_path)
			detected["Mach-O format"] = binary_report.get("format", "Unknown")
			detected["CPU architecture"] = binary_report.get("architecture", "Unknown")
			detected["Binary type"] = binary_report.get("file_type", "Unknown")
			detected["Build platform"] = binary_report.get("platform", "Not specified")
			detected["Linked libraries"] = str(binary_report.get("dependencies", []))
			if not binary_report.get("valid", false):
				detected["Mach-O inspection"] = binary_report.get("error", "Inspection failed")
		detected["Assessment"] = "Static header and dependency inspection only. This does not make the macOS binary executable on Android."
	_draw_page()

func _find_file_path(path: String, wanted: String) -> String:
	var dir := DirAccess.open(path)
	if dir == null:
		return ""
	dir.list_dir_begin()
	var name := dir.get_next()
	while name != "":
		if name == wanted and not dir.current_is_dir():
			dir.list_dir_end()
			return path.path_join(name)
		if dir.current_is_dir() and name != "." and name != "..":
			var found := _find_file_path(path.path_join(name), wanted)
			if found != "":
				dir.list_dir_end()
				return found
		name = dir.get_next()
	dir.list_dir_end()
	return ""

func _generate_plan() -> void:
	var plan := {"product":"Project Dream Android Setup","plan_version":1,"source_folder":source_path,"target":"android-arm64","renderer":renderer,"detected_files":detected,"components":component_checks,"status":"configuration_only_runtime_not_implemented"}
	var out_dir := "user://ProjectDreamBuild"
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(out_dir))
	var file := FileAccess.open(out_dir.path_join("project-dream-build-plan.json"), FileAccess.WRITE)
	if file == null:
		_set_log("ERROR: Could not create plan: " + error_string(FileAccess.get_open_error()))
		return
	file.store_string(JSON.stringify(plan, "\t"))
	file.close()
	_set_log("Build plan generated: " + out_dir.path_join("project-dream-build-plan.json") + "\n\nThis is not a playable game APK. The compatibility runtime is not implemented yet.")

func _set_log(value: String) -> void:
	log_text = value
	if log_view:
		log_view.text = value

func _add_card(title_text: String, detail: String) -> void:
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", _style(Color("#17263A"), 12))
	body.add_child(panel)
	var col := VBoxContainer.new()
	panel.add_child(col)
	var title := Label.new()
	title.text = title_text
	title.add_theme_color_override("font_color", ACCENT)
	col.add_child(title)
	var detail_label := Label.new()
	detail_label.text = detail
	detail_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	detail_label.add_theme_color_override("font_color", Color.WHITE)
	col.add_child(detail_label)

func _add_notice(message: String) -> void:
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", _style(Color("#263247"), 10))
	body.add_child(panel)
	var label := Label.new()
	label.text = message
	label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	label.add_theme_color_override("font_color", Color("#D4E0EF"))
	panel.add_child(label)

func _add_status_row(key: String, value: String) -> void:
	var row := HBoxContainer.new()
	body.add_child(row)
	var left := Label.new()
	left.text = key
	left.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	left.add_theme_color_override("font_color", Color.WHITE)
	row.add_child(left)
	var right := Label.new()
	right.text = value
	right.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	right.add_theme_color_override("font_color", MUTED)
	row.add_child(right)

func _style(color: Color, radius: int) -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = color
	style.corner_radius_top_left = radius
	style.corner_radius_top_right = radius
	style.corner_radius_bottom_left = radius
	style.corner_radius_bottom_right = radius
	style.border_width_left = 1
	style.border_width_right = 1
	style.border_width_top = 1
	style.border_width_bottom = 1
	style.border_color = Color("#354B66")
	style.content_margin_left = 14
	style.content_margin_right = 14
	style.content_margin_top = 12
	style.content_margin_bottom = 12
	return style

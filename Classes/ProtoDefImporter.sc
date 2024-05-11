/*
ProtoDef import mechanism.

.import(path)
- if path starts with / is absolute, otherwise append currentDir
- if path is Quark, import all .proto.scd files in Quark.localPath

when importing a directory, imported def names are tracked, so that a resume of imported defs can be printed

error reports:
"asdf.scd".load can't be try/catched for errors happening in loaded file, which are directly printed to Post window.
So, for our import report, we assume that a "load" that returns nil is an error. This makes empty files count as error as well.
*/
+ProtoDef {

	*import { |path=".", recursive = true, verbose = true| 
		var importedNames, failedDefPaths;

		case 
		{ path.isKindOf(Quark) } { path = path.localPath }
		{ PathName(path).isRelativePath } {
			path = thisProcess.nowExecutingPath.dirname +/+ path
		};

		if(File.exists(path).not) {
			Error("ProtoDef: import % not found".format(path)).throw;
		};

		protect {
			currentImportNames = Set();
			if (PathName(path).isFile) {
				var res = this.prImportFile(path);
				if (res.isNil) { failedDefPaths = [path] }
			} {
				failedDefPaths = this.prImportDirectory(path, recursive);
			}
		} {
			importedNames = currentImportNames.asArray;
			currentImportNames = nil;
		};

		if(verbose) { 
			if (importedNames.size == 0) {
				warn("[ProtoDefs] 0 imported")
			} {
				"*** [ProtoDefs] % imported:".format(importedNames.size).postln;
				importedNames.do{ |name|
					"  * %".format(ProtoDef(name.asSymbol)).postln
				};
			};

			if (failedDefPaths.size > 0) {
				warn(
					"[ProtoDefs] % ".format(failedDefPaths.size) ++
					"import% failed".format(if(failedDefPaths.size != 1, 's', ''))
				);
				failedDefPaths.do(_.warn);
			}{
				"*** [ProtoDefs] no errors".postln;
			};
		};
	}

	// private implementations

	// hook for Protodef.new to register imported names
	*prLogImport { |name|
		if (currentImportNames.notNil) {
			currentImportNames.add(name)
		}
	}

	// import folder 
	*prImportDirectory { |path, recursive = true|
		var emptyImports = [], importedNames;
		var dir = PathName(path);
		var importFunc = { |file|
			if(file.fileName.endsWith(".proto.scd")){
				var res = this.prImportFile(file.fullPath);
				if (res.isNil) {
					emptyImports = emptyImports.add(file.fullPath)
				}
			}
		};

		if (recursive) { dir.filesDo(importFunc) } { dir.files.do(importFunc) };
		// return paths that returned nil for reporting
		^emptyImports
	}

	// import a single file, return load results (path is absolute)
	*prImportFile { |path|
		"*** [ProtoDefs] Loading: %".format(path).postln;
		^path.load;
	}

	/* BROKEN: error catching with String:load seems to never catch anything
	*tryFile { |path|
	var result;
	try {
	"*** [ProtoDefs] Loading: %".format(path).postln;
	result = path.load;
	} { |error|
	^(error: error, path: path);
	}
	^(def: result, path: path);
	}
	*/
}

ProtoDefImporter {

	classvar <libraryPaths;

	*initClass {
		libraryPaths = Set[]
	}

	*addLibraryPath { |...paths|
		paths.do { |p| libraryPaths.add(p) }
	}

	// names can be filenames or directory to import recursively
	*absolute { |paths, baseDir = "", recursive = true, verbose = true|
		^this.prImportFiles(paths, baseDir, recursive, verbose);
	}
	*fromLibrary { |names, recursive = true, verbose = true|
		var results;
		if (names.isString) { names = names.bubble };
		results = names.collect { |path|
			var baseDir = libraryPaths.detect { |dir|
				File.exists(dir +/+ path)
			};
			if (baseDir.isNil) {
				"*** [ProtoDefs] '%' not found in library".format(path).warn;
				(path: path, error: \notFound)
			} {
				this.prImportFiles([path], baseDir, recursive, false)
			}
		};
		if (verbose) {
			this.prPrintImportResults(results.flat.reject(_.isNil))
		};
		^results;
	}
	*relative { |names = $*, baseDir = "protodefs", recursive = true, verbose = true|
		baseDir = thisProcess.nowExecutingPath.dirname +/+ baseDir;
		if(names == $*) {
			^this.directory(baseDir, recursive, verbose)
		} {
			^this.prImportFiles(names, baseDir, recursive, verbose)
		}
	}

	// import folder, yield results, optionally print
	*directory { |path, recursive = true, verbose = true|
		var results = [];
		var dir = PathName(path);
		if(recursive) {
			dir.filesDo{|file|
				if(file.extension == "scd"){
					var path = file.fullPath;
					results = results.add((def: this.file(path), path: path));
				}
			}
		} {
			results = dir.files.collect{|file|
				if(file.extension == "scd"){
					var path = file.fullPath;
					(def: this.file(path), path: path);
				}
			}
		};
		if(verbose) { this.prPrintImportResults(results) };
		^results;
	}

	// import a single file, return load results (path is absolute)
	*file { |path|
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

	// private implementations

	*prImportFiles {|names, baseDir, recursive = true, verbose = true|
		var results;
		names = names.asArray;

		if(names.isEmpty){ ^[] };

		results = names.collect {|name|
			var path = baseDir +/+ name;

			if(File.exists(path).not and: File.exists(path++".scd").not){
				"*** [ProtoDefs] '%' not found in %".format(name, baseDir).warn;
				(path: path, error: \notFound)
			} {
				if(PathName(path).isFolder){
					this.directory(path, recursive: recursive, verbose: false)
				}{
					if(path.extension != "scd"){ path = path ++ ".scd" };
					(def: this.file(path), path: path);
				}
			}
		};
		if(verbose) { this.prPrintImportResults(results.flat.reject(_.isNil)) };
		^results;
	}

	*prPrintImportResults {|results|
		var loadedDefNames, failedDefPaths;
		// "printing %".format(results).postln;

		loadedDefNames  = results.reject{|res| res.def.isNil }.collect{|res| res.def.defName };
		failedDefPaths = results.select{|res| res.def.isNil };

		"*** [ProtoDefs] % Loaded:".format(loadedDefNames.size).postln;
		loadedDefNames.postln;

		if(failedDefPaths.size>0){
			error(
				"[ProtoDefs] % ".format(failedDefPaths.size) ++
				"definition% failed".format(if(failedDefPaths.size!=1){'s'}{''})
			);

			failedDefPaths do: { |failedDef|
				var name = failedDef.path.basename;

				switch(failedDef.error)
				{ \notFound } { "%: no defs were found".format(name).error }
				{
					"%".format(name).error;
					failedDef.error !? { failedDef.error.error }
				};
			}
		}{
			"*** [ProtoDefs] no errors".postln;
		};
	}
}
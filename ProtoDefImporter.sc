ProtoDefImporter {

	// names can be filenames or directory to import recursively
	*absolute { |paths, baseDir = "", verbose = true|
		^this.prImportNames(paths, baseDir, verbose);
	}
	*extension { |names, baseDir = nil, verbose = true|
		baseDir = baseDir ? Platform.userExtensionDir;
		^this.prImportNames(names, baseDir, verbose);
	}
	*relative { |names, baseDir = "", verbose = true|
		baseDir = thisProcess.nowExecutingPath.dirname +/+ baseDir;
		^this.prImportNames(names, baseDir, true);
	}

	// import folder, yield results, optionally print
	*directory { |path, recursive = true, verbose = true|
		var results = [];
		var dir = PathName(path);
		if(recursive) {
			dir.filesDo{|file|
				if(file.extension == "scd"){
					var path = file.fullPath;
					results = results.add((def: this.file(file.fullPath), path: path));
				}
			}
		} {
			results = dir.files.collect{|file|
				if(file.extension == "scd"){
					var path = file.fullPath;
					(def:this.file(path), path:path);
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

	// private implementations

	*prImportNames {|names, baseDir, verbose = true|
		var results;
		names = names.asArray;

		if(names.isEmpty){ ^[] };

		results = names.collect {|name|
			var path = baseDir +/+ name;

			if(File.exists(path).not and: File.exists(path++".scd").not){
				"*** [ProtoDefs] '%' not found in %".format(name, baseDir).warn;
				(path: path)
			} {
				if(PathName(path).isFolder){
					this.directory(path, false)
				}{
					if(path.extension != "scd"){ path = path ++ ".scd" };
					(def:this.file(path), path: path);
				}
			}
		};
		if(verbose) { this.prPrintImportResults(results.flat.reject(_.isNil)) };
		^results;
	}

	*prPrintImportResults {|results|
		var loadedDefNames, failedDefPaths;
		"printing %".format(results).postln;

		loadedDefNames  = results.reject{|res| res.def.isNil }.collect{|res| res.def.defName };
		failedDefPaths = results.select{|res| res.def.isNil }.collect{|res| res.path.basename};

		"*** [ProtoDefs] % Loaded:".format(loadedDefNames.size).postln;
		loadedDefNames.postln;

		if(failedDefPaths.size>0){
			error(
				"[ProtoDefs] % ".format(failedDefPaths.size) ++
				"definition% failed".format(if(failedDefPaths.size!=1){'s'}{''})
			);
			failedDefPaths do: _.error
		}{
			"*** [ProtoDefs] no errors".postln;
		};
	}
}
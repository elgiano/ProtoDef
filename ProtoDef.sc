// Definition of a Prototype
// an Environment that will be referenced by Prototypes

ProtoDef : Environment{

	classvar <defs;
	classvar <import;

	var <>defName;
	var <>parentName;

	*initClass {
		defs = IdentityDictionary.new;
		import = ProtoDefImporter;
	}

	// deprecated
	*loadProtodefs{|dir,dirName="protodefs"|
		"ProtoDef:*loadProtodefs is DEPRECATED. Use Protodef:import instead".warn;
		dir = (dir ? thisProcess.nowExecutingPath.dirname) +/+ dirName;
		dir = dir +/+ "*.scd";
		this.import.absolute(dir.pathMatch);
	}

	*fromObject{|name,copyFrom,defBlock=nil|
		var obj = super.newFrom(copyFrom ? ()).know_(true);
		defs[name] = obj;
		defs[name].defName = name;
		obj[\initDef] !? {
			obj.initDef();
		};

		defBlock !? {
			defs[name].use(defBlock);
		};

		^defs[name].defName;
	}

	*new {|name,defBlock=nil,parent=nil|
		defs[name] = defs[name] ? super.new(know:true);
		defs[name].defName = name;

		defBlock !? {
			defs[name].use(defBlock);
		};

		parent !? {
			defs[name].parentName = parent;
			defs[name].parent = ProtoDef(parent);
		}

		^defs[name];
	}

	/*getClassCode {
	var code = this.defName.asString ++"{\n";
	this.select{|val| val.isFunction}.keysValuesDo{|name,func|
	code = code ++ (name++func.def.sourceCode++";\n");
	};
	code = code ++ "}\n";
	^code;
	}*/
}
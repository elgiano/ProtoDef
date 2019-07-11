// Definition of a prototype
// an Environment that will be referenced by prototypes

ProtoDef : Environment{

	classvar <defs;

	var <>defName;

	*loadProtodefs{|dir|
		dir = dir ? thisProcess.nowExecutingPath.dirname +/+ "protodefs";
		dir = dir +/+ "*.scd";
		dir.loadPaths(action:{ProtoDef.defs.keys.postln})
	}

	*initClass {
        defs = IdentityDictionary.new;
    }

	*new {|name,copyFrom=nil|
		if(copyFrom.notNil){
			var obj = super.newFrom(copyFrom).know_(true);
			defs[name] = obj;
			if(obj.initDef.notNil){
				obj.initDef();
			};
		}{
			defs[name] = defs[name] ? super.new(know:true);
		};

		defs[name].defName = name;
		^defs[name];

	}

	getClassCode {
		var code = this.defName.asString ++"{\n";
		this.select{|val| val.isFunction}.keysValuesDo{|name,func|
			code = code ++ (name++func.def.sourceCode++";\n");
		};
		code = code ++ "}\n";
		^code;
	}


}


// A Prototype references a ProtoDef
// calling its methods and values when it hasn't overridden them with its own
/*
// Define a prototype, and implicitly a ProtoDef called \testProcess
q = Prototype(\testProcess)
ProtoDef(\testProcess).hello = { "Hello World".postln }
q.hello
ProtoDef(\testProcess).hello = {|q| "Hello World, my name is %\n".postf(q.name) }
q.def.name = "Default"
q.hello
q.name = "Specific"
q.hello
q.clear
q.hello
w = Prototype(\testProcess)
w.hello

// define prototype with use:
w.def.use{|q|
   q.init = {|q|
     // in a method, q is the instance, and values are stored in the instance
     q.freq = rrand(20,20000);
     q.synths = List[];
     q.makeSynth = {|q| q.synths.add(Synth(\default,[\freq,q.freq]))};
     q.freeAll = {|q,play=true| q.synths.do{|sy| sy.free}};
   };
   // outside of the method, q is the definition, and values are shared among instances
   q.sharedModulation = 20;
}

// init methods
a init method on the def is called automatically on Prototype instantiation
a initDef method on the def is called automatically on ProtoDef creation
*/

Prototype : Environment{

	var <defName;

	*new{|name|
		^super.new().linkProtoDef(name)
	}

	linkProtoDef{|name|
		defName = name;
		if(this.def.init.notNil){this.init();}
	}

	overrideDef{
		ProtoDef(defName,this);
	}

	def{
		^ProtoDef(defName);
	}

	def_{|name|
		defName = name;
	}

	doesNotUnderstand { arg selector ... args;
		var func;

			func = this[selector];
			if (func.notNil) {
				^func.functionPerformList(\value, this, args);
			};

			if (selector.isSetter) {
				selector = selector.asGetter;
				if(this.respondsTo(selector)) {
					warn(selector.asCompileString
						+ "exists a method name, so you can't use it as pseudo-method.")
				};
				^this[selector] = args[0];
			};
			func = this[\forward];
			if (func.notNil) {
				^func.functionPerformList(\value, this, selector, args);
			};
		// if everything else fails, reference the prototype definition
		^this.referenceDef(*([selector]++[args]))
	}

	referenceDef { arg selector ... args;
		var func = this.def[selector];
		if (func.notNil) {
			^func.functionPerformList(\value, this, *args);
		};
		^nil
	}
}

Prot : Prototype{}
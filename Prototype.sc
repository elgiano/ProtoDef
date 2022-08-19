
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

	*new{ |name, beforeInit, args|
		^super.new().linkProtoDef(name, beforeInit, args)
	}

	linkProtoDef{ |name, beforeInit, initArgs|
		defName = name;
		// beforeInit
		beforeInit !? { this.use(beforeInit) };
		// init
		this.def[\init] !? { this.init(*initArgs ? []) };
	}

	overrideDef {
		ProtoDef.fromObject(defName,this);
	}

	def { ^ProtoDef(defName) }

	def_ {|name| defName = name }

	doesNotUnderstand { arg selector ... args;

		if (selector.isSetter) {
			selector = selector.asGetter;
			if(this.respondsTo(selector)) {
				warn(selector.asCompileString
					+ "exists a method name, so you can't use it as pseudo-method.")
			};
			^this[selector] = args[0];
		};

		^this.prTryProtoMethod(selector, args);
	}

	prTryProtoMethod {|methodName, args|
		var func = this[methodName] ? this.def[methodName];
		var result;

		func ?? { ^nil };
		/*if(this.class.useEnv){
		this.use{ result  = func.valueArray(args) };
		^result;
		}{*/
		{
			result = func.functionPerformList(\value, this, args);
		}.try{|err|
			error("[ProtoDef: %] '%' failed".format(this.defName,methodName));
			err.throw;
		}
		^result;
		//}
	}

	super {|methodName,args|
		var parent, func, result;
		parent = this.def.parent;
		parent ?? {
			"[ProtoDef: %] no parent def to call for 'super.%'"
			.format(this.defName, methodName).warn;
			^nil
		};
		func = this.def.parent[methodName];
		func ?? {
			"[ProtoDef: %] method '%' not found in parent def %"
			.format(this.defName, methodName, this.def.parentName).warn;
			^nil
		};

		try{
			result = func.functionPerformList(\value, this, args);
		} { |err|
			error(
				"[ProtoDef: % : %] '%' failed"
				.format(this.defName, this.def.parentName, methodName)
			);
			err.throw;
		}
		^result;
	}

	update { |...args| ^this.prTryProtoMethod(\update, args)}
	play { |...args| ^this.prTryProtoMethod(\play, args)}
	isPlaying { |...args| ^this.prTryProtoMethod(\isPlaying, args)}
	stop { |...args| ^this.prTryProtoMethod(\stop, args)}
	clear { |...args| ^this.prTryProtoMethod(\clear, args)}
	free { |...args| ^this.prTryProtoMethod(\free, args)}
	release { |...args| ^this.prTryProtoMethod(\free, args)}
	numChannels { |...args| ^this.prTryProtoMethod(\numChannels, args)}
	choose { |...args| ^this.prTryProtoMethod(\choose, args)}

	asString {|...args|
		var protoFunc = this[\asString] ? this.def[\asString];
		protoFunc !? { ^this.prTryProtoMethod(\asString, args) };
		^"Prot('%'%)".format(this.defName, if(this.def.isEmpty){" (empty)"}{""});
	}

	printFields {
		"*** Prototype fields:".postln;
		if(this.keys.isEmpty) { "(empty)".postln } {
			this.keys.do{|k| "%: %".format(k, this[k]).postln}
		};
		"*** ProtoDef fields:".postln;
		if(this.def.keys.isEmpty) { "(empty)".postln } {
			this.def.keys.do{|k| "%: %".format(k, this.def[k]).postln}
		};

	}

}

Prot : Prototype{}

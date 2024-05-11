Prototype : Environment{

	var <defName;

	*new{ |name, beforeInit, args|
		^super.new().prLinkProtoDef(name, beforeInit, args)
	}

	prLinkProtoDef{ |name, beforeInit, initArgs|
		defName = name;
		beforeInit !? { this.use(beforeInit) };
		this.def[\init] !? { this.init(*initArgs ? []) };
	}

	def { ^ProtoDef(defName) }

	def_ {|name| defName = name }

	overrideDef {
		ProtoDef.fromObject(defName,this);
	}

	// a prototype doesNotUnderstand everything
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

	// pseudo-method overrides
	update { |...args| ^this.prTryProtoMethod(\update, args)}
	play { |...args| ^this.prTryProtoMethod(\play, args)}
	isPlaying { |...args| ^this.prTryProtoMethod(\isPlaying, args)}
	stop { |...args| ^this.prTryProtoMethod(\stop, args)}
	clear { |...args| ^this.prTryProtoMethod(\clear, args)}
	free { |...args| ^this.prTryProtoMethod(\free, args)}
	release { |...args| ^this.prTryProtoMethod(\free, args)}
	numChannels { |...args| ^this.prTryProtoMethod(\numChannels, args)}
	choose { |...args| ^this.prTryProtoMethod(\choose, args)}
	count { |...args| ^this.prTryProtoMethod(\count, args)}

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

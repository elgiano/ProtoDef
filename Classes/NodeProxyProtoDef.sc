ProtoNodeProxy : NodeProxy {
	var <>prototype;

	init{
		super.init;
		prototype = ()
	}

	// ask the prototype
	doesNotUnderstand{|...args|

		prototype !? {
			var res = prototype.doesNotUnderstand(*args);
			res !? {^res}
		}
		^super.doesNotUnderstand(*args)
	}

}

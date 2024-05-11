# ProtoDef

Prototyping classes for SuperCollider. sclang class library needs to be recompiled every time a class is changed, which makes it hard to interactively develop classes, especially because recompiling causes a loss of all the current interpreter state. The common work-around is to write pseudo-classes using Environment or its subclasses (e.g. Event). This allows to define an object that contains methods and properties: methods are just Function type properties, that can be called thanks to an hi-jacking of the DoesNotUnderstand error. 

ProtoDef helps organizing this workflow, featuring:
- A global register of ProtoDef definitions
- Initialization functions
- Inheritance
- Import facilities
- Server hooks, class- or instance- wise

See the HelpFiles for examples and instructions

```supercollider
// define a pseudo-class
ProtoDef(\timer) {
	// ~init is called automatically when an instance is created
	~init = { |self, tag|
		self.startTime = Date.localtime.rawSeconds;
        self.tag = self.tag ? "timer";
	};
	
	// return elapsed seconds since self.startTime
	~elapsedTime = { |self|
		Date.localtime.rawSeconds - self.startTime
	};

	// return elapsed seconds and reset startTime to now
	~partial = { |self, tag|
		var partialDur = self.elapsedTime;
        self.tag = tag ? self.tag;
		self.startTime = Date.localtime.rawSeconds;
        "[timer] %: %s".format(tag, partialDur).postln;
		partialDur
	};
};

// create an instance
t = Prot(\timer) { ~tag = "init" };
// call methods defined in ProtoDef
t.partial("blop");
t.elapsedTime.postln;

// add a new method
ProtoDef(\timer) {
	// measure exec time of a function
	~timeBlock = { |self, tag, block|
		var dur;
		protect { 
            self.startTime = Date.localtime.rawSeconds;
			block.value 
        } {
			dur = self.partial(tag)
		};
		dur
	}
}

// now t can measure exec time of a function
t.timeBlock("fft") {
	var size = 2**16;
	var noise = Signal.fill(size){1.0.rand2};
	noise.fft(Signal.newClear(size), Signal.fftCosTable(size))
}

// import defs from other files/folders
ProtoDef.import("path/to/definitions/")
```

**EXPERIMENTAL - WORK IN PROGRESS (FOR EVER?)**
I have been using this utility for some years now, and sometimes I still make changes to its interface. I'm trying to make it nice to use for myself (and potentially others). At the moment I feel it has everything I need and I have written some documentation too. However, I might still change a few things in the future, so be careful when updating.

There are other Quarks out there for the same functionality. In particular <https://github.com/supercollider-quarks/ddwPrototype>

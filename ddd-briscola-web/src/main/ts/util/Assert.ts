module Util {
	
	export function assert(b:boolean, msg:string) {
		if (!b) throw new Error(msg)
	}
	
	export function fail<T>(msg:string):T {
		throw new Error(msg)
	}
	
}
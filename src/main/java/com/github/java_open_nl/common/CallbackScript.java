/* 
	MIT License
	
	Copyright (c) 2017 giannismarinakis
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/
package com.github.java_open_nl.common;

//This class is used to store a script's object that needs to receive incoming RPC calls
public class CallbackScript{
	public Object scriptObject;
	public Class<?> scriptClass;

	public CallbackScript(Object caller){
		try {
			String callerClassName = caller.getClass().getName();

			scriptObject = caller;
			try {
				scriptClass = Class.forName(callerClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			System.out.println("Error while enabling an RPC object. Object passed cannot be null (NullPointerException).");
		}
	}
} 
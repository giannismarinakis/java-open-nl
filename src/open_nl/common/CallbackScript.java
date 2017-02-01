/* 
	Copyright (C) 2017  Giannis Marinakis
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	LICENSE file for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package open_nl.common;

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
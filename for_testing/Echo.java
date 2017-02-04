package for_testing;

import open_nl.common.Sender;
//....dwadw
public class Echo {
	int x;
	public Echo(int x){
		this.x = x;
	}
	
	void test(Sender sender, String me){
		System.out.println(me + " " + x);
	}
}

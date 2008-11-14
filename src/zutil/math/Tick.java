package zutil.math;

public class Tick {

	/**
	 * TEST
	 */
	public static void main(String[] args){
		String temp = "a";
		while(true){
			temp = tick(temp,3);
			System.out.println(temp);
		}
	}
	
	/**
	 * Ticks a given string(increments the string with one)
	 * 
	 * @param ts The string to tick
	 * @param maxChar The maximum number of characters in the string
	 * @return The ticked string
	 */
	public static String tick(String ts, int maxChar){
		StringBuffer ret = new StringBuffer(ts.trim());
		int index = ret.length()-1;
		
		if(ret.length() < maxChar){
			ret.append('a');
		}
		else{
			while(index >= 0){
				char c = increment(ret.charAt(index));
				if(c != 0){
					if(index == 0 && ret.length() < maxChar) ret.append('a');
					if(index == 0) ret = new StringBuffer(""+c);
					else ret.setCharAt(index,c);
					break;
				}
				else{
					//ret.setCharAt(index,'a');
					ret.deleteCharAt(index);
					index--;
				}
			}
		}
		
		return ret.toString();
	}
	
	/**
	 * Increments the char with one after the swedish alfabet
	 * 
	 * @param c The char to increment
	 * @return The incremented char in lowercase 0 if it reached the end
	 */
	public static char increment(char c){
		switch(Character.toLowerCase(c)){
		case 'z': return '�';
		case '�': return '�';
		case '�': return '�';
		}
		c = (char)(Character.toLowerCase(c) + 1);
		if(isAlfa(c)){
			return c;
		}
		return 0;
	}
	
	/**
	 * Checks if the char is a valid character in 
	 * the Swedish alfabet
	 * 
	 * @param c The char to check
	 * @return True if the char is a valid letter 
	 */
	public static boolean isAlfa(char c){
		switch(Character.toLowerCase(c)){
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
		case '�':
		case '�':
		case '�': return true;
		default: return false;
		}
	}
}

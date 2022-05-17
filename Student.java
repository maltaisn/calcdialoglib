public class Student{
	public String name;
	public int roll;

	public Student (String str, int i){
		name=str;
		roll=i;
	}
	public Student(){

	}

	public void setName (String str) {
        name=str;
	}
	public String getName (){
		return name;
	}
}
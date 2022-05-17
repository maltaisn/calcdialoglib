public class Teacher{
	public String name;
	public int id;
        public int age;
	public Teacher (String str, int i){
		name=str;
		id=i;
	}
	public Teacher(){

	}

	public void setName (String str) {
        name=str;
	}
	public String getName (){
		return name;
	}
}

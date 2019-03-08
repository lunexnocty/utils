package test.pers.lunex.utils;

import pers.lunex.utils.argparser.*;

import java.util.ArrayList;
import java.util.Scanner;

@Subcommand(id = "add", parent = "testapp")
class Add extends Parser {
    @Param(id = "num1", required = true, type = Type.INT, arity = -1, index = 0)
    private Integer num1;
    @Param(id = "num2", required = true, type = Type.INT, arity = -1, index = 1)
    private Integer num2;

    public Add(String args) {
        super();
        this.Parse(args);
    }

    public String Execute() {
        if(this.isSuccess) {
            return "the answer is " + (this.num1 + this.num2);
        } else {
            return this.error;
        }
    }
}

@Command (id = "testapp", version = "1.0.0", desc = "this is a test parser.")
public class argpaserTest extends Parser {
    // 注解说明
    // id 为必选，必须给这个参数一个名字，一般与变量名相同
    // names 为选项参数必选，给出选项开关，位置参数可以留空，默认为空数组
    // index 位置参数的位置，如果该参数为位置参数，则此项必选，默认为 0
    // type 参数类型，默认为 BOOL
    // arity 参数的元数，默认为 0，位置参数的 arity 为负数，如 -2 表示该位置参数是一个包含两个元素的数组，3 表示该选项三个值
    // required 是否为必选项，默认 false
    // desc 参数说明，用于生成帮助文档
    @Param(id ="help", names = {"-h", "--help"}, desc = "show help message.", conflicts = { "all" })
    private Boolean help;
    @Param(id = "version", names = {"-v", "--version"}, desc = "show version message.", conflicts = { "all" })
    private Boolean version;
    @Param(id = "name", index = 0, type = Type.STRING, desc = "username.", arity = 1, choices = { "lunex", "vent", "venique" })
    private String name;
    @Param(id = "num", index = 1, type = Type.INT, desc = "a int number.", arity = 1, conflicts = { "intlist" }, required = true)
    private Integer num;
    @Param(id = "def", index = 2, type = Type.INT, desc = "a int number.", arity = 1, defaults = "-121")
    private Integer def;
    @Param(id = "float", names = {"-f", "--float"}, type = Type.FLOAT, desc = "a float number.", arity = 1, defaults = "0.5")
    private Float f;
    @Param(id = "intlist", names = {"-l", "--list"}, type = Type.ARRAY_INT, desc = "a int array.", arity = 3)
    private ArrayList<Integer> intlist;

    public argpaserTest(String args) {
        // 如果需要添加子命令，必须调用父类的构造函数
        super();
        this.addSubcommand(Add.class);
        this.Parse(args);
    }

    public String Execute() {
        String result = "";
        if(this.hasSubcommand()) {
            return this.subcommand.Execute();
        } else if(this.isSuccess) {
            // 如果解析成功
            System.out.println("success");
            if(this.f != null) {
                result += "the parameter <-f> is " + this.f + "\n";
            }
            if(this.name != null) {
                result += "the parameter [name] is " + this.name + "\n";
            }
            if(this.num != null) {
                result += "the parameter [num] is " + this.num + "\n";
            }
            if(this.version != null) {
                result += "the parameter <-v> is " + this.version + "\n";
            }
            if(this.intlist != null) {
                result += "the parameter <-l> is " + this.intlist + "\n";
            }
            if(this.help != null) {
                result += "the parameter <-h> is " + this.help + "\n";
            }
            if(this.def != null) {
                result += "the parameter [def] is " + this.def + "\n";
            }
            return result;
        } else {
            // 解析失败
            return this.error;
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            String cmds = in.nextLine();
            if(cmds.equals("quit")) {
                System.out.println("thanks, exit now...");
                return;
            } else {
                try {
                    Parser parser = new argpaserTest(cmds);
                    System.out.println(parser.Execute());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("main: " + e.getMessage());
                }
            }
        }
    }
}
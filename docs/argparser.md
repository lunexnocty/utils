# argparser
## Introduction
[argparser](./docs/argparser.md) 是一个对用户友好的命令行解析器，用户自定义命令类，通过一个命令字符串生成一个命令对象，返回字符串形式的执行结果。通过注解定义参数、支持可嵌套的子命令、支持开关参数，选项参数与位置参数等多种参数类型、支持参数冲突选项、支持参数缺省值、支持必选参数选项、支持动态参数、输入违法参数时返回友好错误提示信息……

## Getting Started
#### 如何自定义命令类？
很简单，你只需要通过 `Command` 注解你的类，并继承 `Parser` 类，即可获得一个自定义的命令类，通过命令字符串去实例化你的命令类，即可获得一个命令对象。一个简单的例子如下：
```java
@Command (id = "add", version = "1.0.0", desc = "this is an add parser.")
class Add extends Parser {
    public Add(String args) {
        super();
        this.Parse(args);
    }
}
```
#### 如何定义参数？
通过 `Param` 注解定义参数，一个简单的选项参数定义如下：
```java
@Param(id = "nums", names = {"-n", "--number"}, type = Type.ARRAY_INT, dynamic = true, desc = "A integer number.")
private Integer[] nums;
```
#### 如何使用？
首先需要重写 `Parser` 类的 `Execute()` 方法，自定义命令类的行为逻辑，然后即可很方便地通过构造函数来获取命令对象。下面是一个完整的加法例程：
```java
@Command (id = "add", version = "1.0.0", desc = "this is an add parser.")
class Add extends Parser {
    @Param(id = "nums", names = {"-n", "--number"}, type = Type.ARRAY_INT, dynamic = true, desc = "A integer number.")
    private Integer[] nums;
    public Add(String args) {
        super();
        this.Parse(args);
    }
    public String Execute() {
        if (this.isSuccess) {
            if (this.nums != null ) {
                int sum = 0;
                for (auto num : this.nums) {
                    sum += num;
                }
                return "the sum is " + sum;
            }
        } else {
            return this.error;
        }
    }
    public static void main(String[] args) {
        String cmds = "--number 1 2 3 4 5 6";
        Parser add = new Add(cmds);
        System.out.println(add.Execute()); // [OUTPUT]: the sum is 21
    }
}
```
### Prerequisite
Java JDK11+
### Installation
+ [last release(v1.0.0-beta)](./releases/tag/v1.0.0-beta) at 2019-03-03
### Configuration
#### Maven
```xml
<dependency>
    <groupId>utils.lunex.com</groupId>
    <artifactId>utils</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${pom.basedir}/lib/lunex-utils-v1.0.0-20190303-beta.jar</systemPath>
</dependency>
```
#### Gradle
```build.gradle
dependencies { compile files('libs/lunex-utils-v1.0.0-20190303-beta.jar')}
```
## Advanced
### 1. 定义命令类
+ **1.1.** 所有的命令类都需要继承 `Parser` 类，它是所有命令类的基类，负责初始化命令类，解析命令行参数，参数冲突检测等。
+ **1.2.** 命令类还需要使用 `Command` 注解，这个注解仅有三个选项：`id`, `version`, `desc`，分别表示命令类的唯一标识（必选，一般是类名），版本号（默认`"1.0.0"`）以及对命令的描述。
+ **1.3.** 重写构造函数，构造函数内完成对象初始化与参数解析，如果存在子命令需要在解析参数之前添加子命令后再初始化。
```java
@Command(id = "app", version = "1.0.0", desc = "this is a custom commandline arguments parser.")
public class App extends Parser {
    public App(String args) {
        super();
        this.Parse(args);
    }
}
```
### 2. 定义参数
#### 参数注解
`id` 为必选项，是参数的唯一标识，一般和字段标识符（变量名）保持一致，所有参数必选。
`names` 为参数的名字集，可以通过这个集合的字符串来指定该参数，默认为空集，开关参数与选项参数必须指定该选项。
`type` 指定了参数的类型，默认为布尔类型，目前支持八种类型，定义在 `argparser.type` 中，分别为：`INT`, `BOOL`, `STRING`, `FLOAT`, `ARRAY_INT`, `ARRAY_FLOAT`, `ARRAY_BOOL`, `ARRAY_STRING`。
`dynamic` 如果选项为 `true`，则表明该参数的元数个数为动态的，不确定个数的，会贪婪的匹配后面的所有值为该参数的值。
`desc` 为这个参数的描述信息，用于生成帮助文档。
`required` 如果该选项的值为 `true`，则该参数为必选参数。
`index` 如果该参数为位置参数，那么此选项指定该位置参数的位置，默认为 `-1`，位置参数必须指定该选项。
`arity` 指定了参数的元数个数，默认为0，即不带参数的选项，与 `dynamic` 选项冲突。
`conflicts` 为冲突选项集，通过 `id` 指定与该参数相冲突的其他参数。`"all"` 表示该参数与所有参数冲突。
`defaults` 为该参数的缺省值，通过空格分隔多个缺省值。
`choices` 可选集，如果该集合不为空则该参数的值必须在可选集内。
+ **2.1.** 开关参数
    开关参数仅需要指定 `id`, `names` 两个必选选项，其他选项可根据需求自由抉择。
    ```java
    @Param(id = "version", names = {"-v", "--version"}, conflicts = "all")
    private Boolean version;
    ```
+ **2.2.** 选项参数
    选项参数比起开关参数，需要指定参数中元数的个数，由 `arity` 指定，当不知道元数的个数为多少时，通过 `dynamic` 选项来匹配任意多的元数。
    ```java
    @Param(id = "file", names = {"-f", "--file"}, type = Type.STRING, arity = 1)
    private String file;
    ```
+ **2.3.** 位置参数
    位置参数不需要指定 `names` 选项，相应的，必须指定 `index` 选项。
    ```java
    @Param(id = "name", type = Type.STRING, index = 0, arity = 1, defaults = "lunex")
    private String name;
    ```
+ **2.4.** 参数冲突
    参数冲突通过 `conflicts` 选项来指定，默认与任何选项不冲突。
    ```java
    @Param(id = "files", type = Type.ARRAY_STRING, index = 1, dynamic = true, conflicts = "file")
    private String[] files;
    ```
    上述代码说明，`files` 是一个位置参数，接收动态个元数，与 id 为 `file` 参数相冲突，如果命令字符串里同时指定了file与files参数，将抛出异常：`[ConflictArguments]: files cannot be used with file`
#### 子命令
`argparser`支持嵌套的子命令，通过 `Subcommand` 注解来定义一个子命令类，子命令类同样需要继承 `Parser` 类。
`Subcommand` 有四个选项，分别是 `id`, `parent`, `required`, `desc`，分别表示子命令的唯一标识，子命令的父命令 `id`，是否必选，以及对子命令的描述。子命令的定义与主命令类是一样的，并且子命令是允许嵌套的，即子命令可以继续添加子命令的子命令。定义子命令时，必须将子命令注册到父命令中。
例如：
```java
@Subcommand(id = "str", parent = "add", desc = "add strings")
class Str extends Parser {
    @Param(id = "strs", type = Type.ARRAY_STRING, index = 0, dynamic = true)
    private String[] strs;
    public Str(String args) {
        super();
        this.Parse(args);
    }
    public String Execute() {
        if (this.isSuccess) {
            String res = "";
            for(String s : this.strs) {
                res += s;
            }
            return "the result is " + s
        } else {
            return this.error;
        }
    }
}
...
@Command(id = "add", desc = "a add program.")
class Add extends Parser {
    public Add(String args) {
        super();
        this.addSubcommand(Str.class);
        this.Parse(args);
    }
    ...
    public static void main(String[] args) {
        String cmds = "str ab cd ef";
        Parser add = new Add(cmds);
        System.out.println(add.Execute()); // [OUTPUT]: the result is abcdef
    }
}
```
## Change Log
+ 2019-03-03: [v1.0.0-beta](./releases/tag/v1.0.0-beta)
    - 新版特性
        * [x] 通过注解构建命令
        * [x] 支持嵌套的子命令
        * [x] 支持参数冲突
        * [x] 返回字符串形式的执行结果
    - TODO
        * [ ] 自动生成帮助文档
        * [ ] 支持可选集
## Contributors
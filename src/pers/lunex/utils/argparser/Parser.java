package pers.lunex.utils.argparser;

import pers.lunex.utils.exceptions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class Parser {
    private ArrayList<Field> parameters = new ArrayList<>();
    private Map<String, Class<?>> subCommands = new HashMap<>();
    private Map<String, String[]> mutex = new HashMap<>();
    private String id = "";
    protected Parser subcommand = null;
    protected String error = "";
    protected boolean isSuccess = false;

    private static Object Convert(ArrayList<String> values, Type type) throws CannotConvertTypeException, UnSupportedTypeException {
        try {
            Object value;
            switch (type) {
                case ARRAY_INT:
                    value = new ArrayList<Integer>();
                    for(String val : values) {
                        ((ArrayList<Integer>)value).add(Integer.parseInt(val));
                    }
                    return value;
                case ARRAY_FLOAT:
                    value = new ArrayList<Float>();
                    for(String val : values) {
                        ((ArrayList<Float>)value).add(Float.parseFloat(val));
                    }
                    return value;
                case ARRAY_STRING:
                    return values;
                case ARRAY_BOOL:
                    value = new ArrayList<Boolean>();
                    for(String val : values) {
                        ((ArrayList<Boolean>)value).add(Boolean.parseBoolean(val));
                    }
                    return value;
                case INT:
                    return Integer.parseInt(values.get(0));
                case FLOAT:
                    return Float.parseFloat(values.get(0));
                case STRING:
                    return values.get(0);
                case BOOL:
                    return Boolean.parseBoolean(values.get(0));
                default:
                    throw new UnSupportedTypeException("[TypeError]: unsupported parameter type");
            }
        } catch (UnSupportedTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new CannotConvertTypeException("[TypeError]: cannot convert " + values.toString() + " to " + type.toString());
        }

    }

    public Parser() {
        this.Init();
    }

    public String Execute() {
        return "you must override this method in your own parser class.";
    }

    public boolean Parse(String args) {
        ArrayList<String> arguments;
        if(args.equals("")){
            arguments = new ArrayList<>();
        } else {
            arguments = new ArrayList<>(Arrays.asList(args.strip().split(" ")));
        }
        try {
            if(!this.subCommands.isEmpty() && this.subCommands.keySet().contains(arguments.get(0))) {
                String name = arguments.remove(0);
                this.subcommand = this.getSubcommand(name, String.join(" ", arguments));
            }
            for(Field field : this.parameters) {
                Param param = field.getAnnotation(Param.class);
                boolean isExsit = false;
                if(arguments.isEmpty()) {
                    isExsit = false;
                } else if(param.index() < 0) {
                    for(String name : param.names()) {
                        if(arguments.contains(name)) {
                            ArrayList<String> values = new ArrayList<>();
                            isExsit = true;
                            int pos = arguments.indexOf(name);
                            arguments.remove(pos);
                            if(param.arity() == 0) {
                                field.set(this, true);
                            } else {
                                if(param.dynamic()) {
                                    while ( !(arguments.isEmpty() || arguments.get(0).startsWith("-")) ) {
                                        values.add(arguments.remove(0));
                                    }
                                } else {
                                    for (int i = 0; i < param.arity(); i++) {
                                        if (pos >= arguments.size() || arguments.get(pos).startsWith("-")) {
                                            throw new ParserSyntaxException("[SyntaxError]: " + param.id() + " are " + param.arity() + " parameters required but given " + i + " parameters.");
                                        } else {
                                            values.add(arguments.remove(pos));
                                        }
                                    }
                                }
                                field.set(this, Parser.Convert(values, param.type()));
                            }
                        }
                    }
                } else {
                    ArrayList<String> values = new ArrayList<>();
                    if(param.dynamic()) {
                        while ( !(arguments.isEmpty() || arguments.get(0).startsWith("-")) ) {
                            values.add(arguments.remove(0));
                        }
                    } else {
                        for(int i = 0; i < param.arity(); ++i) {
                            if(arguments.isEmpty() || arguments.get(0).startsWith("-")){
                                throw new ParserSyntaxException("[SyntaxError]: " + param.id() + " are " + param.arity() + " parameters required but given " + i + " parameters.");
                            } else {
                                values.add(arguments.remove(0));
                            }
                        }
                    }
                    field.set(this, this.Convert(values, param.type()));
                    isExsit = true;
                }
                if (isExsit) {
                    this.mutex.put(param.id(), param.conflicts());
                } else {
                    if ( !("".equals(param.def())) ) {
                        ArrayList<String> values = new ArrayList<>(Arrays.asList(param.def().strip().split(" ")));
                        field.set(this, this.Convert(values, param.type()));
                    } else if (param.required()) {
                        throw new ParserSyntaxException("[SyntaxError]: " + param.id() + " is necessary.");
                    }
                }
            }
            if( !arguments.isEmpty()) {
                throw new ParserSyntaxException("[SyntaxError]: unrecognized parameter with " + arguments.toString());
            }
            this.Conflict();
            this.isSuccess = true;
            return true;
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            this.error = e.getMessage();
            return false;
        }
    }

    protected boolean addSubcommand(Class<?> cls) {
        try {
            if(cls.isAnnotationPresent(Subcommand.class)) {
                if(cls.getAnnotation(Subcommand.class).parent().equals(this.id)) {
                    this.subCommands.put(cls.getAnnotation(Subcommand.class).id(), cls);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean hasSubcommand() {
        if(this.subcommand == null) {
            return false;
        }
        return true;
    }

    private Parser getSubcommand(String name, String args) {
        try {
            Class cls = this.subCommands.get(name);
            try {
                Constructor constructor = cls.getConstructor(String.class);
                this.subcommand = (Parser) constructor.newInstance(args);
            } catch (NoSuchMethodException e) {
                Constructor constructor = cls.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                this.subcommand = (Parser) constructor.newInstance(args);
            } catch (IllegalAccessException e) {
                Constructor constructor = cls.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                this.subcommand = (Parser) constructor.newInstance(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            return this.subcommand;
        }
    }

    private void Init() {
        try {
            if(this.getClass().isAnnotationPresent(Command.class)) {
                Command program = this.getClass().getAnnotation(Command.class);
                this.id = program.id();
            } else if(this.getClass().isAnnotationPresent(Subcommand.class)) {
                Subcommand subCommand = this.getClass().getAnnotation(Subcommand.class);
                this.id = subCommand.id();
            } else {
                throw new Exception("[ERROR]: undefined command.");
            }
            for(Field field : this.getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(Param.class)) {
                    this.parameters.add(field);
                    field.setAccessible(true);
                }
            }
            this.parameters.sort((o1, o2) -> (((o1.getAnnotation(Param.class).index() - o2.getAnnotation(Param.class).index()) == 0) ? (o1.getAnnotation(Param.class).arity() - o2.getAnnotation(Param.class).arity()) : (o1.getAnnotation(Param.class).index() - o2.getAnnotation(Param.class).index())));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private boolean Conflict() throws ConflictArgumentsException {
        for(Map.Entry<String, String[]> entry : this.mutex.entrySet()) {
            for(String id : entry.getValue()) {
                if ("all".equals(id) && this.mutex.size() > 1) {
                    throw new ConflictArgumentsException("[ConflictArguments]: " + entry.getKey() + " cannot be used with other parameters");
                }
                if(this.mutex.keySet().contains(id)) {
                    throw new ConflictArgumentsException("[ConflictArguments]: " + entry.getKey() + " cannot be used with " + id);
                }
            }
        }
        return true;
    }
}
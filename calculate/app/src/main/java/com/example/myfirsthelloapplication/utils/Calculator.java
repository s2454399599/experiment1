package com.example.myfirsthelloapplication.utils;




import java.util.*;
import java.util.regex.Pattern;


/**
 * 算数表达式求值
 * 直接调用Calculator的类方法conversion()
 * 传入算数表达式，将返回一个浮点值结果
 * 如果计算过程错误，将返回一个NaN
 */
public class Calculator {


    public static double conversion(String expression) {

        double result = 0;
        Calculator cal = new Calculator();
        try {
            expression = transform(expression);
            System.out.println(expression);
            result = cal.calculate(expression);
        } catch (Exception e) {
            // e.printStackTrace();
            // 运算错误返回NaN
            //System.out.println("计算错误，请检查表达式");
            return 0.0 / 0.0;
        }
        // return new String().valueOf(result);
        return result;
    }


    /**
     * 将表达式中负数的符号更改
     *
     * @param expression 例如-2+-1*(-3E-2)-(-1) 被转为 ~2+~1*(~3E~2)-(~1)
     * @return
     */
    private static String transform(String expression) {
        char[] arr = expression.toCharArray();
        String resultstring = null;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '-') {
                if (i == 0) {
                    arr[i] = '~';
                } else {
                    char c = arr[i - 1];
                    if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == 'E' || c == 'e' || c == 's' || c == 'c' || c == '²' || c == '³' || c == '^' || c == 't') {
                        arr[i] = '~';
                    }
                }
            }
        }
        //System.out.println(arr);
        if (arr[0] == '~' || arr[1] == '(') {
            arr[0] = '-';
            resultstring = "0" + new String(arr);
            //若首位数字为负数
            //则将已经转换好的负数的符号变更为'0-**'
        } else {
            resultstring = new String(arr);
        }

        /**
         * 以下为新增代码,表达式最前面的~已被解决，下面使用遍历的方法插入零，
         */
        int strlen = expression.length();
        StringBuilder sb = new StringBuilder(resultstring);
        int index=0;
        char c;
        while (index < strlen){
            c = sb.charAt(index);
            if (c == '~'){
                sb.insert(index,"0");
                index++;
                strlen++;
            }
            index++;
        }
        resultstring = sb.toString();
        resultstring = resultstring.replace("~","-");

        return  resultstring;
    }

    /**
     * 为支持负数运算
     * 将用户可能输入的负数转化为计算机认识的语言
     */


    public double calculate(String expression) throws Exception {

        Stack<Double> numStack = new Stack<Double>();   //用来存放数字的栈
        Stack<Operator> operStack = new Stack<>();  //存放操作符的栈
        //operStack.push(Operator.Getplace);
        Map<String, Operator> computeOper = this.getComputeOper();    //获取运算操作符

        //首先先将三角函数替换为首字符便于分割
        expression = expression.replace("sin", "s");
        expression = expression.replace("cos", "c");
        expression = expression.replace("tan", "t");

        //当前元素
        String currentELe;

        //按照指定分隔符分割字符串并保留分隔符
        StringTokenizer stringTokenizer = new StringTokenizer(expression, "+-*/()^sc", true);
        while (stringTokenizer.hasMoreTokens()) {//遍历分割后的每个元素

            //获取当前去除两边空格元素
            currentELe = stringTokenizer.nextToken().trim();
            //只处理非空字符
            if (!"".equals(currentELe)) {

                //数字直接入数字栈
                if (this.isNum(currentELe)) {
                    currentELe = currentELe.replace("~", "-");
                    numStack.push(Double.parseDouble(currentELe));
                    System.out.println("数字栈目前" + numStack);
                }

                //如果不是数字，则为操作符
                else {
                    //首先获取当前字符串对应的枚举操作符
                    System.out.println("符号栈" + operStack);
                    Operator currentOper = computeOper.get(currentELe);
                    if (currentOper != null) {
                        //说明为运算符而非括号
                        //当栈不空，且栈顶优先级大于等于当前读到时
                        while (!operStack.empty() && operStack.peek().priority() >= currentOper.priority()) {
                            compute(numStack, operStack);
                        }
                        //计算完后把当前操作符加入到操作栈中
                        operStack.push(currentOper);
                        System.out.println("符号栈" + operStack);
                    } else {
                        //则为括号
                        if ("(".equals(currentELe)) {
                            //如果是左括号直接入栈
                            operStack.push(Operator.BRACKETS);

                        } else {
                            //右括号
                            while (!operStack.peek().equals(Operator.BRACKETS)) {
                                compute(numStack, operStack);
                            }
                            //移出左括号
                            operStack.pop();
                        }
                    }


                }
            }
        } // 经过上面代码的遍历后最后的应该是nums里面剩两个数或三个数，operators里面剩一个或两个运算操作符
        while (!operStack.empty()) {
            compute(numStack, operStack);
        }
        return numStack.pop();

    }


    /**
     * 判断是否为算术符号
     *
     * @param c
     * @return
     */
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == 's' || c == 'c';
    }

    /**
     * 判断字符串是不是数字
     *
     * @param str
     * @return
     */
    private boolean isNum(String str) {
        String numRegex = "^\\d+(\\.\\d+)?$";   //数字的正则表达式
        return Pattern.matches(numRegex, str);


    }



    /**
     * 计算函数
     *
     * @param numStack  数字栈
     * @param operStack 符号栈
     */
    private void compute(Stack<Double> numStack, Stack<Operator> operStack) throws EmptyStackException {
        //如果是操作符栈顶是sin
        double computeResult;
        if (operStack.peek() == Operator.SIN) {
            double num = numStack.pop();
            computeResult = operStack.pop().compute(0, num);

        }
        //如果是cos
        else if (operStack.peek() == Operator.COS) {
            double num = numStack.pop();
            computeResult = operStack.pop().compute(0, num);
        }
        //如果是tan
        else if (operStack.peek() == Operator.TAN) {
            double num = numStack.pop();
            computeResult = operStack.pop().compute(0, num);
        } else {//加减乘除运算符
            Double num2 = numStack.pop(); // 弹出数字栈最顶上的数字作为运算的第二个数字
            Double num1 = numStack.pop(); // 弹出数字栈最顶上的数字作为运算的第一个数字
            computeResult = operStack.pop().compute(
                    num1, num2); // 弹出操作栈最顶上的运算符进行计算

        }
        numStack.push(computeResult); // 把计算结果重新放到队列的末端

    }

    /**
     * 获取运算操作符
     *
     * @return
     */
    private Map<String, Operator> getComputeOper() {
        return new HashMap<String, Operator>() { // 运算符
            private static final long serialVersionUID = 7706718608122369958L;

            {
                put("+", Operator.PLUS);
                put("-", Operator.MINUS);
                put("*", Operator.MULTIPLY);
                put("/", Operator.DIVIDE);
                put("^", Operator.POW);
                put("s", Operator.SIN);
                put("c", Operator.COS);
                put("t", Operator.TAN);
                put(",", Operator.Getplace);
            }
        };
    }


    /**
     * 操作符类型,enum枚举类型
     */
    private enum Operator {
        /**
         * 加
         */
        PLUS {
            @Override
            public int priority() {
                return 1;
            }

            @Override
            public double compute(double num1, double num2) {
                return num1 + num2;
            }
        },
        /**
         * 减
         */
        MINUS {
            @Override
            public int priority() {
                return 1;
            }

            @Override
            public double compute(double num1, double num2) {
                return num1 - num2;
            }
        },
        /**
         * 乘
         */
        MULTIPLY {
            @Override
            public int priority() {
                return 2;
            }

            @Override
            public double compute(double num1, double num2) {
                return num1 * num2;
            }
        },
        /**
         * 除
         */
        DIVIDE {
            @Override
            public int priority() {
                return 2;
            }

            @Override
            public double compute(double num1, double num2) {

                return num1 / num2;
            }
        },
        /**
         * 幂运算
         */
        POW {
            @Override
            public int priority() {
                return 3;
            }

            @Override
            public double compute(double num1, double num2) {
                return Math.pow(num1, num2);
            }

        },
        /**
         * sin
         */
        SIN {
            @Override
            public int priority() {
                return 4;
            }

            @Override
            public double compute(double num1, double num2) {
                return ArithHelper.sin(num2 + "");
            }
        },
        /**
         * cos
         */
        COS {
            @Override
            public int priority() {
                return 4;
            }

            @Override
            public double compute(double num1, double num2) {
                return ArithHelper.cos(num2 + "");
            }
        },

        TAN {
            @Override
            public int priority() {
                return 4;
            }

            @Override
            public double compute(double num1, double num2) {
                return ArithHelper.tan(num2 + "");
            }
        },

        /**
         * 逗号但是目前从未使用过
         */
        Getplace {
            @Override
            public int priority() {
                return -1;
            }

            @Override
            public double compute(double num1, double num2) {
                return 0;
            }
        },
        /**
         * 括号
         */
        BRACKETS {
            @Override
            public int priority() {
                return 0;
            }

            @Override
            public double compute(double num1, double num2) {
                return 0;
            }
        };

        /**
         * 对应的优先级
         *
         * @return
         */
        public abstract int priority();

        /**
         * 计算两个数对应的运算结果
         *
         * @param num1 第一个运算数
         * @param num2 第二个运算数
         * @return
         */
        public abstract double compute(double num1, double num2);
    }
}
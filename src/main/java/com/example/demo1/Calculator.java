package com.example.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Calculator {

    private BigDecimal number; // 计算数值
    private String operator; // 运算符: +,-,*,/
    private BigDecimal preAll; // 累计计算值
    private int scale = 2; // 默认精度2位小数

    private List<BigDecimal> historyNumList = new ArrayList<>(); // 计算数值历史记录
    private List<String> historyOperateList = new ArrayList<>(); // 运算符历史记录
    private List<BigDecimal> historyTotalList = new ArrayList<>(); // 累计计算值历史记录

    private int operateIndex = -1; // undo/redo最近操作标记
    private int maxIndex = -1; // undo/redo标记最大值

    public void setNumber(BigDecimal number) {
        //首次计算，累计计算值=计算数值
        if (preAll == null) {
            preAll = number;
        } else {
            this.number = number;
        }
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 计算
     */
    public void calculate() {
        if (preAll == null) {
            preAll = BigDecimal.ZERO;
        }
        if (operator == null) {
            System.out.println("请选择操作!");
            return;
        }
        // 新输入值
        if (number != null) {
            // 累计计算
            BigDecimal ret = calPreAndNow(preAll, operator, number);
            // 根据最近操作标记判断是否处于redo/undo中间过程，并覆盖undo/redo操作记录,更新最近操作标记和标记最大值
            if (this.operateIndex == -1) {
                historyTotalList.add(preAll);
                historyNumList.add(number);
                historyOperateList.add(operator);
            } else {
                this.operateIndex++;
                this.maxIndex = this.operateIndex;
                this.historyTotalList.set(this.operateIndex, ret);
                this.historyNumList.set(this.operateIndex - 1, number);
                this.historyOperateList.set(this.operateIndex - 1, operator);
            }
            preAll = ret;
            operator = null;
            number = null;
        }
    }

    /**
     * 回撤到上一步
     */
    public void undo() {
        // 未进行undo/redo操作,存储最后计算结果
        if (preAll != null && operateIndex == -1) {
            historyTotalList.add(preAll);
            operator = null;
            number = null;
        }
        if (historyTotalList.size() == 0) {
            System.out.println("无操作!");
        } else if (historyTotalList.size() == 1) {
            System.out.println("undo后值:0," + "undo前值:" + preAll);
            preAll = BigDecimal.ZERO;
        } else {
            if (operateIndex == -1) {
                operateIndex = historyOperateList.size() - 1;
            } else {
                if (operateIndex - 1 < 0) {
                    System.out.println("无法再undo!");
                    return;
                }
                operateIndex--;
            }
            cancelPreOperate(historyTotalList.get(operateIndex), historyOperateList.get(operateIndex), historyNumList.get(operateIndex));
        }
    }

    /**
     * 根据回撤进行重做
     */
    public void redo() {
        try {
            if (operateIndex > -1) {
                if (operateIndex + 1 == historyTotalList.size() || operateIndex + 1 == this.maxIndex + 1) {
                    System.out.println("无法再redo!");
                    return;
                }
                operateIndex++;
                redoOperate(historyTotalList.get(operateIndex), historyOperateList.get(operateIndex - 1), historyNumList.get(operateIndex - 1));
            }
        } catch (Exception e) {
            System.out.println("redo异常,operateIndex:" + operateIndex);
        }
    }

    private void redoOperate(BigDecimal redoTotal, String redoOpt, BigDecimal redoNum) {
        System.out.println("redo后值:" + redoTotal.setScale(scale, RoundingMode.HALF_UP) + ",redo前值:" + preAll.setScale(scale, RoundingMode.HALF_UP) + ",redo的操作:" + redoOpt + ",redo操作的值:" + redoNum.setScale(scale, RoundingMode.HALF_UP));
        preAll = redoTotal;
        operator = null;
        number = null;
    }

    private void cancelPreOperate(BigDecimal lastTotal, String lastOpt, BigDecimal lastNum) {
        System.out.println("undo后值:" + lastTotal.setScale(scale, RoundingMode.HALF_UP) + ",undo前值:" + preAll.setScale(scale, RoundingMode.HALF_UP) + ",undo的操作:" + lastOpt + ",undo操作的值:" + lastNum.setScale(scale, RoundingMode.HALF_UP));
        preAll = lastTotal;
        operator = null;
        number = null;
    }

    /**
     * 进行累计计算
     */
    private BigDecimal calPreAndNow(BigDecimal preAll, String operator, BigDecimal number) {
        BigDecimal ret = BigDecimal.ZERO;
        operator = operator == null ? "+" : operator;
        switch (operator) {
            case "+":
                ret = preAll.add(number);
                break;
            case "-":
                ret = preAll.subtract(number).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "*":
                ret = preAll.multiply(number).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "/":
                ret = preAll.divide(number, RoundingMode.HALF_UP);
                break;
        }
        return ret;
    }

    /**
     * 显示操作结果
     */
    public String display() {
        StringBuilder sb = new StringBuilder();
        if (preAll != null) {
            sb.append(preAll.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toString());
        }
        if (operator != null) {
            sb.append(operator);
        }
        if (number != null) {
            sb.append(number.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toString());
        }
        System.out.println(sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setNumber(new BigDecimal(1.20));
        calculator.setOperator("+");
        calculator.setNumber(new BigDecimal(2.50));
        calculator.display();
        calculator.calculate();
        calculator.display();
        calculator.setOperator("*");
        calculator.setNumber(new BigDecimal(3.30));
        calculator.display();
        calculator.calculate();
        calculator.display();
        calculator.undo();
        calculator.display();
        calculator.redo();
        calculator.display();
    }

}
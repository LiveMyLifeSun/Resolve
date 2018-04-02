package com.alemand.spi.test;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
public class LabeledFor {
    public static void main(String[] args) {
        int i = 0;
        outer:
        for (;true;){
            inner:
            for (;i<10;i++){
                System.out.println("i:"+i);
                if(i == 2){
                    System.out.println("continue");
                    continue;
                }
                if(i == 3){
                    System.out.println("break");
                    i++;
                    break ;
                }
                if(i == 7){
                    System.out.println("continue outer");
                    i++;
                    continue outer;//继续外循环
                }
                if(i == 8){
                    System.out.println("break outer");
                    break outer;//结束外边的循环
                }
                for(int k = 0;k < 5; k++){
                    if(k == 3){
                        System.out.println("continue inner");
                        continue inner;
                    }
                }
            }
        }
    }
}

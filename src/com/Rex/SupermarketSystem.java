package com.Rex;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

// 水果接口
interface Fruit {
    String getName();
    BigDecimal getPrice();
    default BigDecimal getDiscountedPrice(PromotionStrategy strategy) {
        return strategy.applyDiscount(this);
    }
}

// 具体水果实现
class Apple implements Fruit {
    @Override public String getName() { return "Apple"; }
    @Override public BigDecimal getPrice() { return new BigDecimal("8.00"); }
}

class Strawberry implements Fruit {
    @Override public String getName() { return "Strawberry"; }
    @Override public BigDecimal getPrice() { return new BigDecimal("13.00"); }
}

class Mango implements Fruit {
    @Override public String getName() { return "Mango"; }
    @Override public BigDecimal getPrice() { return new BigDecimal("20.00"); }
}

// 促销策略接口
interface PromotionStrategy {
    BigDecimal applyDiscount(Fruit fruit);
    BigDecimal applyTotalDiscount(BigDecimal total);
}

// 具体促销策略
class NoPromotion implements PromotionStrategy {
    @Override public BigDecimal applyDiscount(Fruit fruit) { return fruit.getPrice(); }
    @Override public BigDecimal applyTotalDiscount(BigDecimal total) { return total; }
}

class StrawberryDiscount implements PromotionStrategy {
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.8");

    @Override
    public BigDecimal applyDiscount(Fruit fruit) {
        if (fruit instanceof Strawberry) {
            return fruit.getPrice().multiply(DISCOUNT_RATE);
        }
        return fruit.getPrice();
    }

    @Override
    public BigDecimal applyTotalDiscount(BigDecimal total) {
        return total;
    }
}

class FullReduction implements PromotionStrategy {
    private static final BigDecimal THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("10.00");
    private final PromotionStrategy baseStrategy;

    public FullReduction(PromotionStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }

    @Override
    public BigDecimal applyDiscount(Fruit fruit) {
        return baseStrategy.applyDiscount(fruit);
    }

    @Override
    public BigDecimal applyTotalDiscount(BigDecimal total) {
        return total.compareTo(THRESHOLD) >= 0
                ? total.subtract(DISCOUNT_AMOUNT)
                : total;
    }
}

// 购物车类
class ShoppingCart {
    private final Map<Fruit, Integer> items = new HashMap<>();
    private PromotionStrategy strategy;

    public ShoppingCart(PromotionStrategy strategy) {
        this.strategy = strategy;
    }

    public void addItem(Fruit fruit, int quantity) {
        items.put(fruit, items.getOrDefault(fruit, 0) + quantity);
    }

    public BigDecimal calculateTotal() {
        BigDecimal subtotal = items.entrySet().stream()
                .map(entry -> entry.getKey().getDiscountedPrice(strategy)
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return strategy.applyTotalDiscount(subtotal)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

// 测试类
public class SupermarketSystem {
    public static void main(String[] args) {
        // 顾客A：苹果和草莓，无促销
        ShoppingCart cartA = new ShoppingCart(new NoPromotion());
        cartA.addItem(new Apple(), 2);
        cartA.addItem(new Strawberry(), 3);
        System.out.println("顾客A总价: " + cartA.calculateTotal());

        // 顾客B：三种水果，无促销
        ShoppingCart cartB = new ShoppingCart(new NoPromotion());
        cartB.addItem(new Apple(), 2);
        cartB.addItem(new Strawberry(), 3);
        cartB.addItem(new Mango(), 1);
        System.out.println("顾客B总价: " + cartB.calculateTotal());

        // 顾客C：三种水果，草莓8折
        ShoppingCart cartC = new ShoppingCart(new StrawberryDiscount());
        cartC.addItem(new Apple(), 2);
        cartC.addItem(new Strawberry(), 3);
        cartC.addItem(new Mango(), 1);
        System.out.println("顾客C总价: " + cartC.calculateTotal());

        // 顾客D：三种水果，草莓8折+满100减10
        ShoppingCart cartD = new ShoppingCart(new FullReduction(new StrawberryDiscount()));
        cartD.addItem(new Apple(), 5);
        cartD.addItem(new Strawberry(), 5);
        cartD.addItem(new Mango(), 2);
        System.out.println("顾客D总价: " + cartD.calculateTotal());
    }
}


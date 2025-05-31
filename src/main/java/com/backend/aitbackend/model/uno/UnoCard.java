package com.backend.aitbackend.model.uno;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnoCard {
    public enum Color {
        RED, YELLOW, GREEN, BLUE, WILD
    }
    
    public enum Type {
        // Number cards
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
        
        // Action cards
        SKIP, REVERSE, DRAW_TWO,
        
        // Wild cards
        WILD, WILD_DRAW_FOUR
    }
    
    private Color color;
    private Type type;
    private int value;

    // Default constructor for JSON deserialization
    public UnoCard() {
    }

    public UnoCard(Color color, Type type) {
        this.color = color;
        this.type = type;
        this.value = calculateValue();
    }

    private int calculateValue() {
        if (type == null) return 0; // Handle null during deserialization
        
        switch (type) {
            case ZERO: return 0;
            case ONE: return 1;
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case SKIP:
            case REVERSE:
            case DRAW_TWO:
                return 20;
            case WILD:
            case WILD_DRAW_FOUR:
                return 50;
            default:
                return 0;
        }
    }

    public boolean canPlayOn(UnoCard topCard, Color declaredColor) {
        // Wild cards can always be played
        if (this.type == Type.WILD || this.type == Type.WILD_DRAW_FOUR) {
            return true;
        }
        
        // Same color
        if (this.color == topCard.color || this.color == declaredColor) {
            return true;
        }
        
        // Same type (number or action)
        if (this.type == topCard.type) {
            return true;
        }
        
        return false;
    }

    public boolean isActionCard() {
        return type == Type.SKIP || type == Type.REVERSE || type == Type.DRAW_TWO || 
               type == Type.WILD || type == Type.WILD_DRAW_FOUR;
    }

    public boolean isWildCard() {
        return type == Type.WILD || type == Type.WILD_DRAW_FOUR;
    }

    public boolean isDrawCard() {
        return type == Type.DRAW_TWO || type == Type.WILD_DRAW_FOUR;
    }

    // Getters and Setters
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Type getType() {
        return type;
    }    public void setType(Type type) {
        this.type = type;
        this.value = calculateValue(); // Recalculate value when type is set
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (isWildCard()) {
            return type.toString();
        }
        return color + "_" + type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UnoCard unoCard = (UnoCard) obj;
        return color == unoCard.color && type == unoCard.type;
    }

    @Override
    public int hashCode() {
        return color.hashCode() + type.hashCode();
    }
}

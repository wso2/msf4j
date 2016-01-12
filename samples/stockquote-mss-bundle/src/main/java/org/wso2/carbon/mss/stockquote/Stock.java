/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.stockquote;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a stock item. @XmlRootElement is used to support xml conversion.
 */
@SuppressWarnings("unused")
@XmlRootElement
public class Stock {

    private String symbol;
    private String name;
    private double last;
    private double low;
    private double high;

    /**
     * No arg constructor is required for xml marshalling
     */
    public Stock() {
    }

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public Stock(String symbol, String name, double last, double low, double high) {
        this(symbol, name);
        this.last = last;
        this.low = low;
        this.high = high;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHigh() {
        return high;
    }

    public double getLast() {
        return last;
    }

    public double getLow() {
        return low;
    }

    public String getName() {
        return name;
    }
}

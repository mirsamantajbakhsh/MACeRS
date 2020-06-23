/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MACeRS;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mir Saman
 */
public class Result {
    Map<String, Double> result = new HashMap<>();

    @Override
    public String toString() {
        String response = "";
        for (Map.Entry<String, Double> s : result.entrySet()) {
            response += s.getKey() + "\t" + s.getValue() + "\t\t";
        }
        return response;
    }


}

package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * gson类，现在没有用到
 * Created by Nancy on 2017/5/10.
 */
public class GsonTools {

    /**
     * @param jsonString 输入json数据
     * @param attr       key
     * @return double数组或null
     */
    public static Double[] getDoubleArrayAttributeFromJson(String jsonString, String attr) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonElement jElement = jsonObject.get(attr);
        if (jElement != null) {
            JsonArray jsonArray = jElement.getAsJsonArray();
            Double[] result = new Double[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                Double aDouble = jsonArray.get(i).getAsDouble();
                result[i] = aDouble;
            }
            return result;
        } else {
            return null;
        }
    }

}

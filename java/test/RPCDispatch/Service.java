package test.RPCDispatch;

/**
 * Test WebService
 */
public class Service {

    /**
     * Reverse the order of characters in a string
     */
    public String reverseString(String input) throws Exception {
       String result = "";
       for (int i=input.length(); i>0; ) result+=input.charAt(--i);
       return result;
    }

    /**
     * Reverse the order of a struct
     */
    public Data reverseData(Data input) throws Exception {
       Data result = new Data();
       result.setField1(input.getField3());
       result.setField2(reverseString(input.getField2()));
       result.setField3(input.getField1());
       return result;
    }
}

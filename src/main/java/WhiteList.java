public class WhiteList {
    public static boolean isWhite(char character){
        return     (character >= 48 && character <= 57)//English numbers
                || (character >= 64 &&   character <= 90)//English Capital letters
                || (character >= 97 &&   character <= 122)//English lowerCase letters
                || (character >= 1568 && character <= 1617)//arabic and persian letters
                || (character >= 1632 && character <= 1641)//persian numbers
                || (character >= 1646 && character <= 1749)//arabic and persian letters
                || (character >= 1774 && character <= 1792);//arabic and persian letters
    }
}

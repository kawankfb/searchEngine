public class CharacterNormalizer {
    public static String normalizeString(String string){
        StringBuilder temp=new StringBuilder();
        char tempChar;
        for (int i = 0; i <string.length() ; i++) {
            tempChar=string.charAt(i);
            if (WhiteList.isWhite(tempChar))
            temp.append(normalize(tempChar));
            else temp.append(tempChar);
        }
        return temp.toString();
    }

    public static char normalize(char character){


        //normalize alef
        if (character==1571
        ||  character==1573
        ||  character==1649
        ||  character==1650
        ||  character==1651
        ||  character==1653
        )
        return (char)1575;



        //normalize b
        if (character==1646
                ||  character==1659
                ||  character==1664
        )
            return (char)1576;


        //normalize t do noghte
        if (character==1663
                ||  character==1661
                ||  character==1660
                ||  character==1658
                ||  character==1657
        )
            return (char)1578;



            //normalize jim
            if (character==1667
                    ||  character==1668
            )
                return (char)1579;

        //normalize ch
        if (character==1671
        ||character==1727)
            return (char)1670;

        //normalize hah
        if (character==1665
                ||  character==1666
                ||  character==1669
        )
            return (char)1581;


        //normalize dal
        if (character>=1672 && character<=1680 ||character==1774)
            return (char)1583;


        //normalize r
        if (character>=1681 && character<=1687 ||character==1689 ||character==1775)
            return (char)1585;


        //normalize s
        if (character>=1690 && character<=1692)
            return (char)1587;


        //normalize sh
        if (character==1786)
            return (char)1588;



        //normalize sad
        if (character==1693
                ||  character==1694
        )
            return (char)1589;



        //normalize zad
        if (character==1787)
            return (char)1590;



        //normalize tah
        if (character==1695
        )
            return (char)1591;



        //normalize ein
        if (character==1696)
            return (char)1593;



        //normalize ghein
        if (character==1788)
            return (char)1594;



        //normalize f
        if (character>=1697 &&  character<=1702)
            return (char)1601;



        //normalize ghaf
        if (character==1703
                ||  character==1704
                ||  character==1647
        )
            return (char)1602;


        //normalize k
        if (character==1595
                ||  character==1596
                ||  character==1603
                || (character>=1706 && character<=1710)
        )
            return (char)1705;


        //normalize gaf
        if (character>=1712 && character<=1716)
            return (char)1711;


        //normalize lam
        if (character>=1717 && character<=1720)
            return (char)1604;


        //normalize mim
        if (character==1790)
            return (char)1605;

        //normalize noon
        if (character>=1721 && character<=1725)
            return (char)1606;

        //normalize waw
        if (character==1572
                ||  character==1654
                ||  character==1655
                ||  character==1743
                || (character>=1732 && character<=1739)
        )
            return (char)1608;


        //normalize heh
        if (character==1577
                ||  character==1726
                ||  character==1749
                ||  character==1791
                || (character>=1728 && character<=1731)
        )
            return (char)1607;


        //normalize ye
        if (character==1568
                ||  character==1574
                ||  character==1656
                || (character>=1597 && character<=1599)
                || (character>=1609 && character<=1610)
                || (character>=1741 && character<=1742)
                || (character>=1744 && character<=1747)
        )
            return (char)1740;

        //to lowercase latin
        if (character>=65 && character<=90)
            return (char)(character+32);


        return character;
    }
}

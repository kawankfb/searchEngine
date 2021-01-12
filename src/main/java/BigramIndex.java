import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class BigramIndex{
    HashMap<String,Set<String>>bigramTokenPairs;
    Map<String,Integer> distinctTokens;
    Map<String,List<String>> cachedCorrections;
    public BigramIndex(){
        bigramTokenPairs =new HashMap<>();
        distinctTokens=new HashMap<>();
        cachedCorrections=new HashMap<>();
    }

    public void add(String token){
        token='$'+token+'$';
        distinctTokens.putIfAbsent(token,0);
        int count= distinctTokens.get(token)+1;
        distinctTokens.put(token,count);
        if (token.length()<2)
            return;
        String[] bigrams=getBigrams(token);
        for (String bigram : bigrams) {
            bigramTokenPairs.putIfAbsent(bigram, new HashSet<>());
            bigramTokenPairs.get(bigram).add(token);
        }

    }
    private String[] getBigrams(String token){
        if (token.length()<2)
            return new String[0];
        String[] bigrams=new String[token.length()-1];
        for (int i = 0; i <token.length()-1 ; i++) {
            bigrams[i]=token.substring(i,i+2);
        }
        return bigrams;
    }


    public Set<String> getSuggestions(String token){
        token='$'+token+'$';
        Set<String> suggestions=new HashSet<>();
        try{
            long result=0;
            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM token_usage WHERE token=\""+token+"\";");
            while (myResults.next()) {
                result=myResults.getInt(1);
            }if (result==1) {
                    suggestions.add(token);
                    return suggestions;
            }
        }
        catch (Exception e){}
            String[] bigrams=getBigrams(token);
        for (String bigram : bigrams) {

            suggestions.addAll(getBigramTokenPairs(bigram));
        }
        return suggestions;
    }

    private Set<String> getBigramTokenPairs(String bigram){
        Set<String> suggestions=new HashSet<>();
        try {
            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT token FROM bigrams WHERE bigram=\""+bigram+"\";");
            while (myResults.next()) {
                String token = myResults.getString(1);
                suggestions.add(token);
            }
        }
        catch (Exception e){}
        return suggestions;
    }

    public long getTokenUsageCount(String token){
        long result=0;
        try{
            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM token_usage WHERE token=\""+token+"\";");
            while (myResults.next()) {
                result=myResults.getInt(1);
            }if (result==1) {
                long tokenCount = 1;
                statement = Main.getConnection().createStatement();
                myResults = statement.executeQuery("SELECT count FROM token_usage WHERE token=\"" + token + "\";");
                while (myResults.next()) {
                    tokenCount = myResults.getLong(1);
                    return tokenCount;
                }
            }}catch (Exception e){

        }
        return result;
    }

    public List<String> getTopSuggestions(String token){
        if (cachedCorrections.containsKey(token)){
            return cachedCorrections.get(token);
        }
        int n=10;//top n suggestions
        Set<String> suggestions=getSuggestions(token);
        List<String> topSuggestions=new ArrayList<>();


        if (suggestions.size()<2) {
            if (suggestions.size()<1)
            {
                cachedCorrections.putIfAbsent(token,topSuggestions);
                return topSuggestions;
            }
            for (String suggestion : suggestions) {
                if (suggestion.startsWith("$"))
                topSuggestions.add(suggestion.substring(1,suggestion.length()-1));
                else topSuggestions.add(suggestion);
            }

            cachedCorrections.putIfAbsent(token,topSuggestions);
            return topSuggestions;
        }

        token='$'+token+'$';


        int count=0;
        int[] editDistance=new int[n];
        Arrays.fill(editDistance, 1000000);
        long[] usageCount=new long[n];
        String[] tokens=new String[n];
        for (String suggestion : suggestions) {
            if (count<n){
                count++;}
            int i;
            int ed=EditDistance.calculateDistance(token,suggestion);//edit distance
            long tuc=getTokenUsageCount(suggestion);//token usage count
            for (i = 0; i <count ; i++) {
                if (ed<=editDistance[i])
                    break;
            }

            for (; i <count ; i++) {
                if (tuc>=usageCount[i] || ed<editDistance[i])
                    break;
            }
            if (i==count && i<n)
            {tokens[i]=suggestion;
            editDistance[i]=ed;
            usageCount[i]=tuc;
            }
            else if (i<count){
                for (int j = count-1; j >i ; j--) {
                    editDistance[j]=editDistance[j-1];
                    usageCount[j]=usageCount[j-1];
                    tokens[j]=tokens[j-1];
                }
                editDistance[i]=ed;
                usageCount[i]=tuc;
                tokens[i]=suggestion;
            }

        }
        for (int i = 0; i <count ; i++) {
           topSuggestions.add(tokens[i].substring(1,tokens[i].length()-1));
        }
        cachedCorrections.putIfAbsent(token,topSuggestions);
        return topSuggestions;
    }

    public String getBestSuggestion(String token){
        if (token.length()<2)
            return token;
        List<String> top=getTopSuggestions(token);
        if (top.isEmpty())
            return token;
        else {
            String temp=top.get(0);
            if (temp.startsWith("$"))
                return temp.substring(1,temp.length()-1);
            else return temp;
        }

    }
    public List<String> getTopQuerySuggestions(String query,int maxSuggestionCount){
        String[] tokens=query.split(" ");
        HashMap<String,List<String>> table=new HashMap<>();

        for (String token : tokens) {
            if (token.equals("AND") ||token.equals("NOT") ||token.equals("OR"))
            {
                List<String> temp = new ArrayList<>();
                temp.add(token);
                table.putIfAbsent(token,temp);
            }
            else {
                String corrected_token=getBestSuggestion(token);
                if (corrected_token.equals(token)) {
                    List<String> temp = new ArrayList<>();
                    temp.add(token);
                    table.putIfAbsent(token,temp);
                }
                else {
                table.putIfAbsent(token,getTopSuggestions(token));
                }
            }
        }

        ArrayList<String> topQuerySuggestions=new ArrayList<>();
        String[][] permutations=new String[tokens.length][];
        for (int i = 0; i <permutations.length ; i++) {
            List<String> tokenSuggestions=table.get(tokens[i]);
            permutations[i]=new String[tokenSuggestions.size()];
            int j=0;
            for (String tokenSuggestion : tokenSuggestions) {
                permutations[i][j]=tokenSuggestion;
                j++;
            }
        }

        int length= permutations.length;
        int[] ints=new int[length];
        int[] limits=new int[length];
        for (int i = 0; i <limits.length ; i++) {
            limits[i]=permutations[i].length-1;
        }
        while (ints[0]<=limits[0] && topQuerySuggestions.size()<maxSuggestionCount)
        {
            StringBuilder tempSB=new StringBuilder();
            int turn=0;
            for (int i : ints) {
                tempSB.append(permutations[turn][i]);
                tempSB.append(' ');
                turn++;
            }
            topQuerySuggestions.add(tempSB.toString());
            ints[length-1]++;
            for (int i = length-1; i >=0 ; i--) {
                if (ints[i]>limits[i]) {
                    if (i==0)
                        break;
                    ints[i - 1]++;
                    ints[i]=0;
                }
            }
        }

        return  topQuerySuggestions;
    }
    public void insertBigramIndexToDB(){
        for (String s : bigramTokenPairs.keySet()) {
            for (String s1 : bigramTokenPairs.get(s)) {
                try {
                    insertBigramTokenPairToDB(s,s1);
                }
                catch (Exception e){
                }
            }
            }
        for (String s : distinctTokens.keySet()) {
            try {
                updateTokenUsageCount(s,distinctTokens.get(s));
            } catch (Exception e) {

            }
        }
        }
        private void insertBigramTokenPairToDB(String bigram,String token) throws Exception{
            Statement statement = Main.getConnection().createStatement();
            StringBuilder queryBuilder=new StringBuilder("INSERT INTO bigrams (bigram, token) VALUES ('");
            queryBuilder.append(bigram);
            queryBuilder.append("','");
            queryBuilder.append(token);
            queryBuilder.append("');");
            statement.execute(queryBuilder.toString());
        }
        private void updateTokenUsageCount(String token,int count) throws Exception {
            int result=0;

            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM token_usage WHERE token=\""+token+"\";");
            while (myResults.next()) {
                result=myResults.getInt(1);
            }
            if (result==0){
                statement = Main.getConnection().createStatement();
               StringBuilder queryBuilder=new StringBuilder("INSERT INTO token_usage (token, count) VALUES ('");
                queryBuilder.append(token);
                queryBuilder.append("','");
                queryBuilder.append(count);
                queryBuilder.append("');");
                statement.execute(queryBuilder.toString());
            }
            else if (result==1){
                long tokenCount=1;
                statement = Main.getConnection().createStatement();
                myResults = statement.executeQuery("SELECT count FROM token_usage WHERE token=\""+token+"\";");
                while (myResults.next()) {
                    tokenCount=myResults.getLong(1);
                }
                statement = Main.getConnection().createStatement();
               StringBuilder queryBuilder=new StringBuilder("UPDATE token_usage SET count=");
                queryBuilder.append(tokenCount+count);
                queryBuilder.append(" WHERE token=\"");
                queryBuilder.append(token);
                queryBuilder.append("\";");

                statement.execute(queryBuilder.toString());
            }
        }


}

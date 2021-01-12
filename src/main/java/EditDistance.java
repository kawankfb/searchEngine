public class EditDistance {
    public static int calculateDistance(String a,String b){
        int[][] distances=new int[a.length()+1][b.length()+1];

        distances[0][0]=0;
        for (int i = 1; i <a.length()+1 ; i++) {
            distances[i][0]=i;
        }

        for (int i = 1; i <b.length()+1 ; i++) {
            distances[0][i]=i;
        }

        for (int i = 1; i <a.length()+1 ; i++) {
            for (int j = 1; j <b.length()+1 ; j++) {
                 if (a.charAt(i-1)==b.charAt(j-1))
                {
                   distances[i][j]= distances[i-1][j-1];
                   if ((distances[i-1][j]+1)<distances[i][j])
                       distances[i][j]=distances[i-1][j]+1;
                    if ((distances[i][j-1]+1)<distances[i][j])
                        distances[i][j]=distances[i][j-1]+1;
                }
                 else {
                     distances[i][j]= distances[i-1][j-1]+1;
                     if ((distances[i-1][j]+1)<distances[i][j])
                         distances[i][j]=distances[i-1][j]+1;
                     if ((distances[i][j-1]+1)<distances[i][j])
                         distances[i][j]=distances[i][j-1]+1;
                 }
            }
        }
        return distances[a.length()][b.length()];
    }
}

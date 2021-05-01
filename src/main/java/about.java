import events.aboutevent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;

public class about {

    public static void main(String args[]) throws Exception{

        JDA jda = new JDABuilder("ODM3ODczNTI2MzYyODY1Njc1.YIy4hw.kxwmuTN0n1Ba6zdxkFAIJZKI7xo").build();

        jda.addEventListener(new aboutevent());
    }


}

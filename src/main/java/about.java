import events.AboutEvent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;

public class about {

    public static void main(String args[]) throws Exception{

        JDA jda = new JDABuilder("ODM3OTIyMTc5MjkwNzU5MjA4.YIzl1w.zxiD1l_7_dH_c3WFQYojw-g4dk4").build();

        jda.addEventListener(new AboutEvent());
    }


}

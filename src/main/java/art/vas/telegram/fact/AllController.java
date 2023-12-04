package art.vas.telegram.fact;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
public class AllController {
    public static final Pattern regexp = Pattern.compile("(?iux)\\b(?:\n" +
            "(?:(?:у|[нз]а|(?:хитро|не)?вз?[ыьъ]|с[ьъ]|(?:и|ра)[зс]ъ?|(?:о[тб]|п[оа]д)[ьъ]?|(?:.\\B)+?[оаеи-])-?)?(?:\n" +
            "  [её](?:б(?!о[рй]|рач)|п[уа](?:ц|тс))|\n" +
            "  и[пб][ае][тцд][ьъ]\n" +
            ").*?|\n" +
            "\n" +
            "(?:(?:н[иеа]|ра[зс]|[зд]?[ао](?:т|дн[оа])?|с(?:м[еи])?|а[пб]ч)-?)?ху(?:[яйиеёю]|л+и(?!ган)).*?|\n" +
            "\n" +
            "бл(?:[эя]|еа?)(?:[дт][ьъ]?)?|\n" +
            "\n" +
            "\\S*?(?:\n" +
            "  п(?:\n" +
            "    [иеё]зд|\n" +
            "    ид[аое]?р|\n" +
            "    ед(?:р(?!о)|[аое]р|ик)\n" +
            "  )|\n" +
            "  бля(?:[дбц]|тс)|\n" +
            "  [ое]ху[яйиеёю]|\n" +
            "  хуйн\n" +
            ").*?|\n" +
            "\n" +
            "(?:о[тб]?|про|на|вы)?м(?:\n" +
            "  анд(?:[ауеыи](?:л(?:и[сзщ])?[ауеиы])?|ой|[ао]в.*?|юк(?:ов|[ауи])?|е[нт]ь|ища)|\n" +
            "  уд(?:[яаиое].+?|е?н(?:[ьюия]|ей))|\n" +
            "  [ао]л[ао]ф[ьъ](?:[яиюе]|[еёо]й)\n" +
            ")|\n" +
            "\n" +
            "елд[ауые].*?|\n" +
            "ля[тд]ь|\n" +
            "(?:[нз]а|по)х\n" +
            ")\\b");

    @GetMapping
    public Object hello() {
        return "hello";
    }

    @GetMapping("stop")
    public Object stop(@RequestParam String word) {
        return regexp.matcher(word).matches();
    }
}

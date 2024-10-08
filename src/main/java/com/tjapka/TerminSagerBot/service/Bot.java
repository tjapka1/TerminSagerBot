package com.tjapka.TerminSagerBot.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.tjapka.TerminSagerBot.Entity.Birthday;
import com.tjapka.TerminSagerBot.Entity.MessageToSend;
import com.tjapka.TerminSagerBot.Entity.Reminder;
import com.tjapka.TerminSagerBot.Entity.Termin;
import com.tjapka.TerminSagerBot.Entity.User;
import com.tjapka.TerminSagerBot.Repository.BirthdayRepository;
import com.tjapka.TerminSagerBot.Repository.ReminderRepository;
import com.tjapka.TerminSagerBot.Repository.TerminRepository;
import com.tjapka.TerminSagerBot.Repository.UserRepository;
import com.tjapka.TerminSagerBot.config.BotConfig;

import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@Component
@Slf4j
@PropertySource("sheduling.properties")
public class Bot extends TelegramLongPollingBot {
    static final String HELP_TEXT_START_ENGL = """
            This bot is created to demonstrate Spring capabilities.

            You can execute commands from the main menu on the left or by typing a command:\s

            This is Start\s

            Type /start to see a welcome message

            Type /help to see this message again""";

    @Autowired
    BotConfig config;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TerminRepository terminRepository;
    @Autowired
    BirthdayRepository birthdayRepository;
    @Autowired
    ReminderRepository reminderRepository;
    static List<String> selectedCommands = new ArrayList<>();
    static List<String> entityId = new ArrayList<>();
    static List<MessageToSend> messageStack = new ArrayList<>();

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");


    public Bot(BotConfig config) {

        this.config = config;

        //Меню Комманд

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get my Data storage"));
        listOfCommands.add(new BotCommand("/newtermin", "set new Termin"));
        listOfCommands.add(new BotCommand("/newreminder", "set new reminder"));
        listOfCommands.add(new BotCommand("/newbirthday", "set new Birthday"));
        listOfCommands.add(new BotCommand("/showtermin", "View alls Termins"));
        listOfCommands.add(new BotCommand("/showbirthday", "View alls Birthday"));
        listOfCommands.add(new BotCommand("/showreminder", "View alls reminder"));
        listOfCommands.add(new BotCommand("/deletetermin", "delete one Termin"));
        listOfCommands.add(new BotCommand("/deletebirthday", "delete one birthday"));
        listOfCommands.add(new BotCommand("/deletereminder", "delete one reminder"));
        listOfCommands.add(new BotCommand("/help", "Info how to use this Bot"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bots command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            handleMessage(update.getMessage());
        }

    }

    private void handleMessage(Message message) {
        long chatId = message.getChatId();
        String userName = message.getFrom().getUserName();
        String fnkBTN = "";

        String messageTextCheck = message.getText();
        String messageText;
        if (messageTextCheck.contains("@TjapkaTerminsager88bot") || messageTextCheck.contains("@TjapkaTerminsager88Testbot")) {
            int i = messageTextCheck.indexOf("@");
            messageText = messageTextCheck.substring(0, i);

        } else {
            messageText = messageTextCheck;
        }

        if (messageText.contains("/onrem")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/onrem";
            onThisReminder(chatId, entityId.get(0));
            clearSelectedcommend();
        }
        if (messageText.contains("/offrem")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/offrem";
            offThisReminder(chatId, entityId.get(0));
            clearSelectedcommend();
        }
        if (messageText.contains("/reditminutes")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/reditminutes";
        }
        if (messageText.contains("/reditname")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/reditname";
        }
        if (messageText.contains("/redittime")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/redittime";
        }
        if (messageText.contains("/reditdays")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/reditdays";
        }
        if (messageText.contains("/beditfirstname")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/beditfirstname";

        }
        if (messageText.contains("/beditlastname")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/beditlastname";

        }
        if (messageText.contains("/beditdate")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/beditdate";

        }
        if (messageText.contains("/beditfordays")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/beditfordays";

        }
        if (messageText.contains("/teditname")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/teditname";

        }
        if (messageText.contains("/teditdate")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/teditdate";
        }
        if (messageText.contains("/teditminusmin")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/teditminusmin";
        }
        if (messageText.contains("/teditfordays")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/teditfordays";
        }
        if (messageText.contains("/tedittime")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/tedittime";

        }
        if (messageText.contains("/deletethisreminder")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/deletethisreminder";
            deleteThisReminder(chatId, entityId.get(0));
            clearSelectedcommend();
        }

        if (messageText.contains("/deletethisbirthday")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/deletethisbirthday";
            deleteThisBirthday(chatId, entityId.get(0));
            clearSelectedcommend();
        }
        if (messageText.contains("/deletethistermin")) {
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s + 1));
            messageText = "/deletethistermin";
            deleteThisTermin(chatId, entityId.get(0));
            clearSelectedcommend();
        }


        //------------------------------------------------
        List<String> fnkStart = List.of("/start", "/help");
        List<String> fnkUser = new ArrayList<>(List.of("/registrade", "/mydata"));
        List<String> fnkUserQ = List.of("/setregion", "/setlanguage", "/deleteuserdata", "/username", "/firstname", "/lastname", "/beditforeverydaystime", "/beditfornowdaytime", "/teditforeverydaystime");
        fnkUser.addAll(fnkUserQ);
        List<String> fnkTermin = List.of("/newtermin", "/showtermin", "/deletetermin", "/deletethistermin", "/teditname", "/teditdate", "/teditminusmin", "/tedittime", "/teditfordays");
        List<String> fnkBirthDay = List.of("/newbirthday", "/showbirthday", "/deletebirthday", "/deletethisbirthday", "/beditfirstname", "/beditlastname", "/beditdate", "/beditfordays");
        List<String> fnkReminder = List.of("/newreminder", "/showreminder", "/deletereminder", "/deletethisreminder", "/reditname", "/redittime", "/reditdays", "/reditminutes", "/onrem", "/offrem");
        //----------------
        List<String> fnkAll = new ArrayList<>();
        fnkAll.addAll(fnkStart);
        fnkAll.addAll(fnkTermin);

        fnkAll.addAll(fnkUser);
        fnkAll.addAll(fnkBirthDay);
        fnkAll.addAll(fnkReminder);
        //------------------------------------------------

        List<String> parser = List.of(messageText.split(" "));
        String fnkCheck = parser.get(0);

        if (messageText.contains("/")) {
            fnkBTN = messageText;
        }

        if (parser.size() > 2) {
            fnkBTN = fnkCheck;
        }

        //Проверка на не существуюшейся функции
        if (messageText.contains("/") && !fnkAll.contains(fnkCheck)) {
            fnkBTN = "";
            defaultComand(chatId, userName, messageText);
        }
        if (messageText.equals("/newtermin")) {
            selectedCommands.add("/newtermin");
        }
        if (messageText.equals("/deletetermin")) {
            selectedCommands.add("/deletetermin");
        }
        if (messageText.equals("/newbirthday")) {
            selectedCommands.add("/newbirthday");
        }
        if (messageText.equals("/deletebirthday")) {
            selectedCommands.add("/deletebirthday");
        }
        if (messageText.equals("/newreminder")) {
            selectedCommands.add("/newreminder");
        }
        if (messageText.equals("/deletereminder")) {
            selectedCommands.add("/deletereminder");
        }
        if (messageText.equals("/setregion")) {
            selectedCommands.add("/setregion");
        }
        if (messageText.equals("/setlanguage")) {
            selectedCommands.add("/setlanguage");
        }
        if (messageText.equals("/deleteuserdata")) {
            selectedCommands.add("/deleteuserdata");
        }
        if (messageText.equals("/username")) {
            selectedCommands.add("/username");
        }
        if (messageText.equals("/firstname")) {
            selectedCommands.add("/firstname");
        }
        if (messageText.equals("/lastname")) {
            selectedCommands.add("/lastname");
        }
        if (messageText.equals("/beditforeverydaystime")) {
            selectedCommands.add("/beditforeverydaystime");
        }
        if (messageText.equals("/beditfornowdaytime")) {
            selectedCommands.add("/beditfornowdaytime");
        }
        if (messageText.equals("/teditforeverydaystime")) {
            selectedCommands.add("/teditforeverydaystime");
        }
        if (messageText.equals("/reditminutes")) {
            selectedCommands.add("/reditminutes");
        }
        if (messageText.equals("/reditname")) {
            selectedCommands.add("/reditname");
        }
        if (messageText.equals("/redittime")) {
            selectedCommands.add("/redittime");
        }
        if (messageText.equals("/reditdays")) {
            selectedCommands.add("/reditdays");
        }
        if (messageText.equals("/beditfirstname")) {
            selectedCommands.add("/beditfirstname");
        }
        if (messageText.equals("/beditlastname")) {
            selectedCommands.add("/beditlastname");
        }
        if (messageText.equals("/beditdate")) {
            selectedCommands.add("/beditdate");
        }
        if (messageText.equals("/beditfordays")) {
            selectedCommands.add("/beditfordays");
        }
        if (messageText.equals("/teditname")) {
            selectedCommands.add("/teditname");
        }
        if (messageText.equals("/teditdate")) {
            selectedCommands.add("/teditdate");
        }
        if (messageText.equals("/tedittime")) {
            selectedCommands.add("/tedittime");
        }
        if (messageText.equals("/teditminusmin")) {
            selectedCommands.add("/teditminusmin");
        }
        if (messageText.equals("/teditfordays")) {
            selectedCommands.add("/teditfordays");
        }


        //------------------------------------------------
        if (fnkStart.contains(fnkBTN)) {
            startFunk(message, fnkBTN, chatId, userName);
            selectedCommands.clear();

        } else if (fnkUser.contains(fnkBTN)) {
            userFunk(message, fnkBTN, chatId, userName);


        } else if (fnkBirthDay.contains(fnkBTN)) {
            birthDayFunk(fnkBTN, chatId, userName);

        } else if (fnkReminder.contains(fnkBTN)) {
            reminderFunk(fnkBTN, chatId, userName);

        } else if (fnkTermin.contains(fnkBTN)) {
            try {
                terminFunk(fnkBTN, chatId, userName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        } else if (!messageText.contains("/") && !fnkAll.contains(fnkCheck) && selectedCommands.isEmpty()) {
            //buildMessage(chatId, "Echo Answer: " + messageText );
            //log.info(userName + ": Type this message: " + messageText);

        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newtermin")) {
            buildNewTermin(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletetermin")) {
            deleteTermin(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newbirthday")) {
            buildNewBirthday(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletebirthday")) {
            deleteBirthday(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newreminder")) {
            buildNewReminder(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletereminder")) {
            deleteOneReminder(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletethisreminder")) {
            deleteOneReminder(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/setregion")) {
            setUserRegion(message, chatId);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deleteuserdata")) {
            deleteUserData(chatId, messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/username")) {
            setUsername(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/firstname")) {
            setfirstname(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/lastname")) {
            setLastname(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/setlanguage")) {
            setLanguage(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditforeverydaystime")) {
            setTimeForEveryDayBirthday(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditfornowdaytime")) {
            setTimeForNowDayBirthday(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditforeverydaystime")) {
            setTimeTerminForEveryDays(chatId, message);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditminutes")) {
            editRemindermindMinuts(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditname")) {
            editRemindermindName(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/redittime")) {
            editRemindermindTime(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditdays")) {
            editRemindermindDays(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditfirstname")) {
            editBirthdayFirstname(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditlastname")) {
            editBirthdayLastname(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditdate")) {
            editBirthdayDate(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditfordays")) {
            editBirthdayForDays(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditname")) {
            editTerminName(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditdate")) {
            editTerminDate(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/tedittime")) {
            editTerminTime(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditminusmin")) {
            editTerminMinusMin(chatId, entityId.get(0), messageText);
        } else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditfordays")) {
            editTerminForDays(chatId, entityId.get(0), messageText);
        }
        //------------------------------------------------
    }


    //Menus
    private void startFunk(Message message, String messageText, long chatId, String userName) {
        switch (messageText) {
            case "/start":
                registerUser(message);
                start(chatId);
                break;
            case "/help":
                help(chatId);
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    // TODO: 12.01.2024 userFunk switch
    private void userFunk(Message message, String messageText, long chatId, String userName) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        switch (messageText) {
            case "/registrade":
                registerUser(message);
                break;
            case "/username":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your username." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ваш ник" : "");
                break;
            case "/firstname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your firstname." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ваш Имя" : "");
                break;
            case "/lastname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your lastname." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите вашу Фамилию" : "");
                break;
            case "/beditforeverydaystime":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter time for birthday reminder ever Days." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите время когда будет приходить напоминание о дне рождение до его дня" : "");
                break;
            case "/beditfornowdaytime":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter time for birthday reminder now Days." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите время когда будет приходить напоминание о дне рождение в этот день" : "");
                break;
            case "/teditforeverydaystime":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter time for termin reminder ever Days." :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите время когда будет приходить напоминание о Термине до его дня" : "");
                break;

            case "/mydata":
                myDataUser(chatId);
                clearSelectedcommend();
                break;
            case "/setregion":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "please set your region\n" + regionsMapToString(getRegionsMap()) :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ваш регион\n" + regionsMapToString(getRegionsMap()) : "");
                break;
            case "/setlanguage":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "please set your language\nEnglish\nRussian" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ваш язык\nEnglish\nRussian" : "");
                break;
            case "/deleteuserdata":

                Long userId = user.get().getId();
                String idLenght = userId.toString();
                String userIdEit = userId.toString().substring(idLenght.length() - 4, idLenght.length());
                long userIdCheck = Long.parseLong(userIdEit);
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "do you want to delete all your data? \nPlease enter this number\n" + userIdCheck :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Вы хотите Удалить все ваши данные? \nНаберите этот номмер\n" + userIdCheck : "");
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    private void terminFunk(String fnkBTN, long chatId, String userName) throws ParseException {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        switch (fnkBTN) {
            case "/newtermin":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your termin! \nExample:  \nTermin Name, Termin Date, Termin Time\nTermin Name, 00.00.0000, 00:00"
                        : user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ваш Термин! \nНапример: \nимя термина, дату термина, время термина \nИмя термина, 00.00.0000, 00:00" : "");
                break;
            case "/showtermin":
                showAllTermins(chatId, user.get().getLanguage());
                break;
            case "/teditname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter new Termin Name" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите имя термина" : "");
                break;
            case "/teditdate":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter new date for Termin" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите Дату термина" : "");
                break;
            case "/tedittime":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter new time for Termin" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите время термина" : "");
                break;
            case "/teditminusmin":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Minutes for Terminn" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите за сколько минут до времени приходили напоминания" +
                                "\nНапример 10 или 20, 10. \nЗа час надо писать в минутах например 60, 75, 120" : "");
                break;
            case "/teditfordays":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Days for Terminn" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите за сколько дней до дня термина приходили напоминания" : "");
                break;
            case "/deletethistermin":
                break;
            case "/deletetermin":
                if (!terminRepository.findByUserId(chatId).isEmpty()) {
                    showAllTermins(chatId, user.get().getLanguage());
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your terminID Of delete" :
                            user.get().getLanguage().toLowerCase().equals("russian") ? "Введите ID термины которые хотите удалить" : "");
                } else {
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "You have not Termins" :
                            user.get().getLanguage().toLowerCase().equals("russian") ? "У вас нет Терминов" : "");
                    clearSelectedcommend();
                }
                break;
            default:
                defaultComand(chatId, userName, fnkBTN);
                break;
        }
    }

    private void birthDayFunk(String messageText, long chatId, String userName) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        switch (messageText) {
            case "/newbirthday":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter new Birthday! " +
                        "\nExample:  " +
                        "\nPerson Firstname, Person Lastname, Birthday Date" +
                        "\n\nExample Firstname, Example Lastname, 00.00.0000"
                        : user.get().getLanguage().toLowerCase().equals("russian") ? "Введите новый день Рождения " +
                        "\nНапример: " +
                        "\nИмя, Фамилия, Дата " +
                        "\n\nНе забывайте про запятые!!!!!!!!" : "");
                break;
            case "/showbirthday":
                showAllBirthday(chatId, user.get().getLanguage());
                break;
            case "/beditfirstname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Birthday new Firstname" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите имя человека" : "");
                break;
            case "/beditlastname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Birthday new Lastname" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите фамилию человека" : "");
                break;
            case "/beditdate":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Birthday new Date" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите день рождения" : "");
                break;
            case "/beditfordays":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter Birthday new days for Birthday" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Введите за сколько дней хотите получать уведомление" : "");
                break;
            case "/deletebirthday":
                if (!birthdayRepository.findByUserId(chatId).isEmpty()) {
                    showAllBirthday(chatId, user.get().getLanguage());
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your BirthDayID Of delete" :
                            user.get().getLanguage().toLowerCase().equals("russian") ? "Введите Id Дней Рождений которые хотите удалить" : "");
                } else {
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "You have not Birthdays" :
                            user.get().getLanguage().toLowerCase().equals("russian") ? "У вас Нет дней рождений" : "");
                    clearSelectedcommend();
                }
                break;
            case "/deletethisbirthday":
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    private void reminderFunk(String messageText, long chatId, String userName) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        switch (messageText) {
            case "/newreminder":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"""
                        Please enter new Reminder!
                        WeekDays
                        1 = Monday
                        2 = Tuesday
                        3 = Wednesday
                        4 = Thursday
                        5 = Friday
                        6 = Saturday
                        7 = Sunday
                        all = for all WeekDays
                        workdays = for Work Days
                        weekend = for Weekend Days\s
                        Example: \s
                        Reminder name, Reminder days, Reminder time
                        Reminder name, 1 3 5 6 , 00:00

                        Reminder name, all, 00:00

                        Reminder name, workdays, 00:00

                        Reminder name, weekend, 00:00"""
                                : user.get().getLanguage().toLowerCase().equals("russian") ?"""
                        Введите ваш напоминатель
                        
                        Дни недели
                        1 = Понедельник
                        2 = Вторник
                        3 = Среда
                        4 = Четверг
                        5 = Пятница
                        6 = Суббота
                        7 = Воскресенье
                        all = для всех дней
                        workdays = для робочих дней
                        weekend = для выходных дней\s
                        Например: \s
                        Имя напоминаний, дни напоминаний, Время напоминаний
                        Имя напоминаний, 1 3 5 6 , 00:00

                        Имя напоминаний, all, 00:00

                        Имя напоминаний, workdays, 00:00

                        Имя напоминаний, weekend, 00:00""":""
                        );
                break;
            case "/showreminder":
                showAllReminder(chatId);
                break;
            case "/deletereminder":
                if (!reminderRepository.findByUserId(chatId).isEmpty()) {
                    showAllReminder(chatId);
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "Please enter your ReminderID Of delete" :
                            user.get().getLanguage().toLowerCase().equals("russian") ?"Введите ID напоминаний которые хотите удалить":"");
                } else {
                    buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"You have not Reminder":
                            user.get().getLanguage().toLowerCase().equals("russian") ?"У вас нет напоминаний":"");
                    clearSelectedcommend();
                }
                break;
            case "/reditminutes":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"Please enter your Reminder minutes" :
                        user.get().getLanguage().toLowerCase().equals("russian") ?"Введите минуты напоминания":"");
                break;
            case "/reditname":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"Please enter your Reminder new name":
                        user.get().getLanguage().toLowerCase().equals("russian") ?"Введите новое имя напоминания":"");
                break;
            case "/redittime":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"Please enter your Reminder new time" :
                        user.get().getLanguage().toLowerCase().equals("russian") ?"Введите новое время напоминания":"");
                break;
            case "/deletethisreminder", "/offrem", "/onrem":
                break;
            case "/reditdays":
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? """
                        Please enter your Reminder new days

                        WeekDays
                        1 = Monday
                        2 = Tuesday
                        3 = Wednesday
                        4 = Thursday
                        5 = Friday
                        6 = Saturday
                        7 = Sunday
                        all = for all WeekDays
                        workdays = for Work Days
                        weekend = for Weekend Days""" :
                        user.get().getLanguage().toLowerCase().equals("russian") ?"""
                        Введите новые дни напоминаний
                        
                        Дни недели
                        1 = Понедельник
                        2 = Вторник
                        3 = Среда
                        4 = Четверг
                        5 = Пятница
                        6 = Суббота
                        7 = Воскресенье
                        all = для всех дней
                        workdays = для робочих дней
                        weekend = для выходных дней\s
                        
                        """:"");
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    private void buildNewTermin(Message message, long chatId) {

        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        String terminName = wordFirstCharToLower(parser.get(0).trim());
        String dateCheck = parser.get(1).trim();
        String timeCheck = parser.get(2).trim();


        String checkDataAll = dateCheckFnk(chatId, dateCheck);
        List<String> dataTrueFalse = List.of(checkDataAll.split("&"));
        List<String> trueFalseDate = List.of(dataTrueFalse.get(1).split("/"));
        String setDate = dataTrueFalse.get(0);

        String checktimeAll = timeCheckFnk(chatId, timeCheck);
        List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
        List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
        String setTime = timeTrueFalse.get(0);


        if (trueFalseDate.get(0).equals("true") && trueFalseDate.get(1).equals("true")
                && trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {
            addTermin(chatId, terminName, setDate, setTime);
        }
        if (trueFalseDate.get(0).equals("false") && trueFalseDate.get(1).equals("false")) {
            String answerLog = "User set false Date";
            log.info(answerLog);
        }
        if (trueFalseTime.get(0).equals("false") && trueFalseTime.get(1).equals("false")) {
            String answerLog = "User set false Time";
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void buildNewBirthday(Message message, long chatId) {
        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        String firstName = wordFirstCharToLower(parser.get(0).trim());
        String lastName = wordFirstCharToLower(parser.get(1).trim());
        String dataCheck = parser.get(2).trim();


        String checkDataAll = dateCheckFnk(chatId, dataCheck);
        List<String> dataTrueFalse = List.of(checkDataAll.split("&"));
        List<String> trueFalse = List.of(dataTrueFalse.get(1).split("/"));
        String setDate = dataTrueFalse.get(0);


        if (trueFalse.get(0).equals("true") && trueFalse.get(1).equals("true")) {
            addBirthday(chatId, firstName, lastName, setDate);
        } else {
            String answerLog = "User set false Date";
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void buildNewReminder(Message message, long chatId) {
        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        String reminderTittle = wordFirstCharToLower(parser.get(0).trim());
        String reminderDaysCheck = parser.get(1).trim();
        String timeCheck = parser.get(2).trim();

        String reminderDays;
        if (reminderDaysCheck.equalsIgnoreCase("all")) {
            reminderDays = "1 2 3 4 5 6 7";
        } else if (reminderDaysCheck.equalsIgnoreCase("workdays")) {
            reminderDays = "1 2 3 4 5";
        } else if (reminderDaysCheck.equalsIgnoreCase("weekend")) {
            reminderDays = "6 7";
        } else {
            reminderDays = reminderDaysCheck;
        }

        String dayChecked = reminderCheckDays(chatId, reminderDays);


        String checktimeAll = timeCheckFnk(chatId, timeCheck);
        List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
        List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
        String setTime = timeTrueFalse.get(0);

        if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {
            String answer = "Use" +
                    "r: " + message.getFrom().getUserName() + " " +
                    "created a new Reminder: " + reminderTittle + " " + dayChecked + " " + setTime;
            log.info(answer);
            addReminder(chatId, reminderTittle, dayChecked, setTime);

            if (trueFalseTime.get(0).equals("false") && trueFalseTime.get(1).equals("false")) {
                String answerLog = "User set false Time";
                log.info(answerLog);
            }
        }
        selectedCommands.clear();
    }


    private void addTermin(long chatId, String terminName, String terminDate, String terminTime) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user = Optional.of(userRepository.findById(chatId).get());

            Termin termin = Termin.builder()
                    .user(user.get())
                    .terminName(terminName)
                    .terminDate(terminDate)
                    .terminTime(terminTime)
                    .terminMinusMin("5 10 15")
                    .terminMinusDay("0")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            Termin save = terminRepository.save(termin);

            String[] dateSplit = dateCheckFnk(chatId, termin.getTerminDate()).split("&");
            List<String> dateComp = List.of(dateSplit[0].split("\\."));
            LocalDate terminDateL = LocalDate.of(Integer.parseInt(dateComp.get(2)), Integer.parseInt(dateComp.get(1)), Integer.parseInt(dateComp.get(0)));

            String[] timeSplit = timeCheckFnk(chatId, termin.getTerminTime()).split("&");
            List<String> timeComp = List.of(timeSplit[0].split(":"));
            LocalTime terminTimeL = LocalTime.of(Integer.parseInt(timeComp.get(0)), Integer.parseInt(timeComp.get(1)));


            if (ChronoUnit.DAYS.between(LocalDate.now(), terminDateL) == 0) {
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Termin")
                        .messageToSend("")
                        .messageToLog("")
                        .time(terminTimeL)
                        .build());
            }

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? user.get().getUserName() + " Termin saved!!!\n" + answerTermin(save, user.get().getLanguage())
                    : user.get().getLanguage().toLowerCase().equals("russian") ? user.get().getUserName() + " Термин сохранён\n" + answerTermin(save, user.get().getLanguage()):"";
            String answerLog = user.get().getUserName() + " Termin saved: " + terminName;
            log.info(answerLog);
            buildMessage(chatId, answer);
        }
    }

    private void addBirthday(long chatId, String firstName, String lastName, String setDate) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user = Optional.of(userRepository.findById(chatId).get());
            Birthday birthday = Birthday.builder()
                    .birthdayFirstName(firstName)
                    .birthdayLastName(lastName)
                    .birthdayDate(setDate)
                    .birthdayMinusDay("1 3 5")
                    .user(user.get())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            Birthday save = birthdayRepository.save(birthday);

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? user.get().getUserName() + " | Birthday saved!!!\n" + answerBirthday(save, user.get().getLanguage())
                    : user.get().getLanguage().toLowerCase().equals("russian") ? user.get().getUserName() + " | День Рождения сохранён!!!\n" + answerBirthday(save, user.get().getLanguage()):"";
            String answerLog = user.get().getUserName() + " | Birthday saved: " + firstName + " " + lastName + " " + setDate;
            log.info(answerLog);
            buildMessage(chatId, answer);
        }
    }

    private void addReminder(long chatId, String reminderTittle, String reminderDays, String reminderTime) {
        //Reminder
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            Reminder reminder = Reminder.builder()
                    .reminderOnOff("on")
                    .reminderTittle(reminderTittle)
                    .reminderDays(reminderDays)
                    .reminderTime(reminderTime)
                    .reminderMinusMin("5 10 15")
                    .user(user.get())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();

            Reminder save = reminderRepository.save(reminder);
            String answer = user.get().getUserName() + " | Reminder saved!!!\n" + answerReminder(save);
            String answerLog = user.get().getUserName() + " | Reminder saved: " + reminderTittle + " " + reminderDays + " " + reminderTime;


            log.info(answerLog);
            buildMessage(chatId, answer);
        }
    }

    private void showAllTermins(long chatId, String language) {
        List<Termin> userTermins = terminRepository.findByUserId(chatId);
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        List<Termin> sortedList = userTermins.stream()
                .sorted(Comparator.comparing(Termin::getTerminDate))
                .sorted(Comparator.comparing(Termin::getTerminTime))
                .toList();


        String answer;

        if (!userTermins.isEmpty()) {
            for (Termin termin : sortedList) {
                answer = answerTermin(termin, language);
                buildMessage(chatId, answer);

            }
            log.info("User show your List of all Termins: " + userTermins.size());
        } else {
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "you have not Termins" : user.get().getLanguage().toLowerCase().equals("russian") ? "У вас нет Терминов" : "";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void showAllBirthday(long chatId, String language) {
        List<Birthday> userBirthdays = birthdayRepository.findByUserId(chatId);
        List<Birthday> sortedList = userBirthdays.stream()
                .sorted(Comparator.comparing(Birthday::getBirthdayDate))
                .toList();

        String answer1;
        String answerLog;
        if (!userBirthdays.isEmpty()) {
            for (Birthday birthday : sortedList) {
                answer1 = answerBirthday(birthday, language);
                buildMessage(chatId, answer1);
            }
            log.info("User show your List of all Birthdays: " + userBirthdays.size());
        } else {
            answer1 = language.toLowerCase().equals("englisch") ? "you have not Birthdays" : language.toLowerCase().equals("russian") ? "У вас Нет дней рождений" : "";
            answerLog = "you have not Birthdays";
            log.info(answerLog);
            buildMessage(chatId, answer1);
        }
    }

    private void showAllReminder(long chatId) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        List<Reminder> userReminder = reminderRepository.findByUserId(chatId);
        List<Reminder> sortedList = userReminder.stream()
                .sorted(Comparator.comparing(Reminder::getReminderTime))
                .toList();

        String answer;
        if (!userReminder.isEmpty()) {
            for (Reminder reminder : sortedList) {
                answer = answerReminder(reminder);

                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Reminders: " + userReminder.size());
        } else {
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "you have not Reminders"
                    : user.get().getLanguage().toLowerCase().equals("russian") ?"У вас нет напоминаний":"";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void editRemindermindMinuts(Long chatId, String editremindermind, String messageText) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()) {
            reminder.get().setReminderMinusMin(messageText);
            reminderRepository.save(reminder.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "reminder edit Minutes: " + reminder.get().getReminderMinusMin() + "\n" +
                    answerReminder(reminder.get()) : user.get().getLanguage().toLowerCase().equals("russian") ?
                    "Изменить минуты через сколько будет приходить оповешение: " + reminder.get().getReminderMinusMin() + "\n" +
                    answerReminder(reminder.get()):"";

            String answerLog = "reminder edit Minutes: " + reminder.get().getReminderMinusMin() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();

    }

    private void editRemindermindName(long chatId, String editremindermind, String reminderName) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!reminderName.isEmpty()) {
            reminder.get().setReminderTittle(wordFirstCharToLower(reminderName));
            reminderRepository.save(reminder.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ?"reminder edit name: " + reminder.get().getReminderTittle() + "\n" +
                    answerReminder(reminder.get()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Изменить имя Оповещения: " + reminder.get().getReminderTittle() + "\n" +
                            answerReminder(reminder.get()):"";

            String answerLog = "reminder edit Name: " + reminder.get().getReminderTittle() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();

    }

    private void editRemindermindTime(long chatId, String editremindermind, String timeReminder) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        String answer = "";
        String answerLog = "";

        if (!timeReminder.isEmpty()) {
            String timeCheck = timeReminder.trim();

            String checktimeAll = timeCheckFnk(chatId, timeCheck);
            List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
            List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
            String setTime = timeTrueFalse.get(0);

            reminder.get().setReminderTime(setTime);
            reminderRepository.save(reminder.get());

            if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {

                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Reminder edit Reminder time: " + reminder.get().getReminderTime() + "\n" +
                        answerReminder(reminder.get()) :
                        user.get().getLanguage().toLowerCase().equals("russian") ?"Пользователь изменил время оповешиния: " + reminder.get().getReminderTime() + "\n" +
                        answerReminder(reminder.get()):"";

                answerLog = "Reminder edit Reminder time: " + reminder.get().getReminderTime() + " ID: " + reminder.get().getId();
            }

            if (trueFalseTime.get(0).equals("false") || trueFalseTime.get(1).equals("false")) {

                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Reminder time: " + reminder.get().getReminderTime() + " | /redittime_" + reminder.get().getId() :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Время оповешиния: " + reminder.get().getReminderTime() + " | /redittime_" + reminder.get().getId():"";
                answerLog = "User set false Time";

            }


        }
        buildMessage(chatId, answer);
        log.info(answerLog);
        clearSelectedcommend();
    }

    private void editRemindermindDays(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!messageText.isEmpty()) {
            String reminderDays;
            if (messageText.equalsIgnoreCase("all")) {
                reminderDays = "1 2 3 4 5 6 7";
            } else if (messageText.equalsIgnoreCase("workdays")) {
                reminderDays = "1 2 3 4 5";
            } else if (messageText.equalsIgnoreCase("weekend")) {
                reminderDays = "6 7";
            } else {
                reminderDays = messageText;
            }

            String daysChecked = reminderCheckDays(chatId, reminderDays);

            reminder.get().setReminderDays(daysChecked);
            reminderRepository.save(reminder.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "reminder edit days: " + reminder.get().getReminderDays() + "\n" +
                    answerReminder(reminder.get()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Изменены дни оповешений: " + reminder.get().getReminderDays() + "\n" +
                            answerReminder(reminder.get()):"";
            String answerLog = "reminder edit Days: " + reminder.get().getReminderDays() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }

    private String reminderCheckDays(Long chatId, String reminderDays) {
        List<String> checkDaysAll = List.of(reminderDays.trim().split(" "));
        List<Integer> daysTrue = new ArrayList<>();
        List<Integer> daysFalse = new ArrayList<>();
        String res;

        for (String day : checkDaysAll) {
            int dayInt = Integer.parseInt(day);
            if (dayInt > 0 && dayInt < 8) {
                daysTrue.add(dayInt);
            } else {
                daysFalse.add(dayInt);

            }
        }
        if (!daysFalse.isEmpty()) {
            String dayf = daysFalse.toString().trim().replace("[", "").replace("]", "").replace(",", "");
            String falseAnswer = "you entered the wrong days of the week!!! " + dayf;

            buildMessage(chatId, falseAnswer);
            log.error(chatId + falseAnswer);
            daysFalse.clear();
        }
        if (daysTrue.isEmpty()) {
            daysTrue.addAll(List.of(1, 2, 3, 4, 5, 6, 7));
        }

        res = daysTrue.toString().trim().replace("[", "").replace("]", "").replace(",", "");

        return res;
    }

    private void onThisReminder(long chatId, String reminderId) {
        if (reminderRepository.findById(Long.parseLong(reminderId)).isPresent()) {

            Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(reminderId));

            reminder.get().setReminderOnOff("on");
            reminderRepository.save(reminder.get());

            String answer = "Reminder is On:\n" +
                    answerReminder(reminder.get());
            String answerLog = "reminder is on : " + reminder.get().getReminderTittle() + " ID : " + reminder.get().getId();


            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void offThisReminder(long chatId, String reminderId) {
        if (reminderRepository.findById(Long.parseLong(reminderId)).isPresent()) {

            Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(reminderId));

            reminder.get().setReminderOnOff("off");
            reminderRepository.save(reminder.get());

            String answer = "Reminder is Off:\n" +
                    answerReminder(reminder.get());
            String answerLog = "reminder is off: " + reminder.get().getReminderTittle() + " ID : " + reminder.get().getId();


            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }

    private void editBirthdayFirstname(long chatId, String birthdayId, String firstName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!firstName.isEmpty()) {
            birthday.get().setBirthdayFirstName(wordFirstCharToLower(firstName));
            birthdayRepository.save(birthday.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "birthday edit Firstname: " + birthday.get().getBirthdayFirstName() + "\n" +
                        answerBirthday(birthday.get(), user.get().getLanguage()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Изменино имя именинника: " + birthday.get().getBirthdayFirstName() + "\n" +
                            answerBirthday(birthday.get(), user.get().getLanguage()):"";

            String answerLog = "birthday edit Firstname: " + birthday.get().getBirthdayFirstName() + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void editBirthdayLastname(long chatId, String birthdayId, String lastName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!lastName.isEmpty()) {
            birthday.get().setBirthdayLastName(wordFirstCharToLower(lastName));
            birthdayRepository.save(birthday.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "birthday edit Lastname: " + birthday.get().getBirthdayLastName() + "\n" +
                    answerBirthday(birthday.get(), user.get().getLanguage()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Изменино Фамилия именинника: " + birthday.get().getBirthdayLastName() + "\n" +
                            answerBirthday(birthday.get(), user.get().getLanguage()):"";

            String answerLog = "birthday edit Lastname: " + birthday.get().getBirthdayLastName() + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editBirthdayDate(long chatId, String birthdayId, String birthdayDateCheck) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!birthdayDateCheck.isEmpty()) {

            String checkDataAll = dateCheckFnk(chatId, birthdayDateCheck);

            List<String> dataTrueFalse = List.of(checkDataAll.split("&"));


            List<String> trueFalse = List.of(dataTrueFalse.get(1).split("/"));

            String setDate = dataTrueFalse.get(0);

            birthday.get().setBirthdayDate(setDate);
            birthdayRepository.save(birthday.get());

            String answer;
            String answerLog;
            if (trueFalse.get(0).equals("true") && trueFalse.get(1).equals("true")) {

                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Birthday edit Birthday date: " + birthday.get().getBirthdayDate() + "\n" +
                        answerBirthday(birthday.get(), user.get().getLanguage()) :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Изменена дата рождения имениника: " + birthday.get().getBirthdayDate() + "\n" +
                        answerBirthday(birthday.get(), user.get().getLanguage()):"" ;

                answerLog = "Birthday edit Birthday0 date: " + birthday.get().getBirthdayDate() + " ID: " + birthday.get().getId();

            } else {
                answer = "Termin date: " + birthday.get().getBirthdayDate() + " | /beditdate_" + birthday.get().getId();
                answerLog = "User set false Date";

            }

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editBirthdayForDays(long chatId, String birthdayId, String birthdayMinusDays) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!birthdayMinusDays.isEmpty()) {
            birthday.get().setBirthdayMinusDay(birthdayMinusDays);
            birthdayRepository.save(birthday.get());

            String answerStandart = user.get().getLanguage().toLowerCase().equals("englisch") ?
                    "birthday edit for birthday days: " + birthday.get().getBirthdayMinusDay() :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Изменили за сколько дней приходили напоминания до дня рождения: " + birthday.get().getBirthdayMinusDay() :"";


            String answer = answerStandart + "\n" +
                    answerBirthday(birthday.get(), user.get().getLanguage());

            String answerLog = answerStandart + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();


    }

    private void editTerminName(long chatId, String terminId, String terminName) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        if (!terminName.isEmpty()) {
            termin.get().setTerminName(terminName);
            terminRepository.save(termin.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin edit Termin name: " + termin.get().getTerminName() + "\n" +
                    answerTermin(termin.get(), user.get().getLanguage()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Вы изменили имя термина: " + termin.get().getTerminName() + "\n" +
                            answerTermin(termin.get(), user.get().getLanguage()) : "";

            String answerLog = "Termin edit Termin name: " + termin.get().getTerminName() + " ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void editTerminDate(long chatId, String terminId, String terminDateCheck) {
        String answer = "";
        String answerLog = "";
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        if (!terminDateCheck.isEmpty()) {

            String checkDataAll = dateCheckFnk(chatId, terminDateCheck);

            List<String> dataTrueFalse = List.of(checkDataAll.split("&"));


            List<String> trueFalse = List.of(dataTrueFalse.get(1).split("/"));

            String setDate = dataTrueFalse.get(0);

            termin.get().setTerminDate(setDate);
            terminRepository.save(termin.get());

            if (trueFalse.get(0).equals("true") && trueFalse.get(1).equals("true")) {

                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin edit Termin date: " + termin.get().getTerminDate() + "\n" +
                        answerTermin(termin.get(), user.get().getLanguage()) :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Вы изменили дату термина" + termin.get().getTerminDate() + "\n" +
                                answerTermin(termin.get(), user.get().getLanguage()) : "";

                answerLog = "Termin edit Termin date: " + termin.get().getTerminDate() + " ID: " + termin.get().getId();

            } else {
                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin date: " + termin.get().getTerminDate() + " | /teditdate_" + termin.get().getId() :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Дата Термина" + termin.get().getTerminDate() + " | /teditdate_" + termin.get().getId() : "";
                answerLog = "User set false Date";

            }
        }
        buildMessage(chatId, answer);
        log.info(answerLog);
        clearSelectedcommend();
    }

    private void editTerminTime(long chatId, String terminId, String terminTime) {
        String answer = "";
        String answerLog = "";
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!terminTime.isEmpty()) {

            String checktimeAll = timeCheckFnk(chatId, terminTime);
            List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
            List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
            String setTime = timeTrueFalse.get(0);

            termin.get().setTerminTime(setTime);
            terminRepository.save(termin.get());

            if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {


                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin edit Termin time: " + termin.get().getTerminTime() + "\n" +
                        answerTermin(termin.get(), user.get().getLanguage()) :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Вы изменили Время термина " + termin.get().getTerminTime() + "\n" +
                                answerTermin(termin.get(), user.get().getLanguage()) : "";

                answerLog = "Termin edit Termin time: " + termin.get().getTerminTime() + " ID: " + termin.get().getId();

            }

            if (trueFalseTime.get(0).equals("false") || trueFalseTime.get(1).equals("false")) {
                answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin time: " + termin.get().getTerminTime() :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Время Термина" + termin.get().getTerminTime() : "" + " | /tedittime_" + termin.get().getId();
                answerLog = "User set false time";

            }
        }
        buildMessage(chatId, answer);
        log.info(answerLog);
        clearSelectedcommend();
    }

    private void editTerminMinusMin(long chatId, String terminId, String terminMinusMin) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!terminMinusMin.isEmpty()) {
            termin.get().setTerminMinusMin(terminMinusMin);
            terminRepository.save(termin.get());

            String an = answerTermin(termin.get(), user.get().getLanguage());
            System.out.println(an);
            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin edit Termin Minutes: " + termin.get().getTerminMinusMin() + "\n" + an :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Вы изменили Минуты до Термина " + termin.get().getTerminMinusMin() + "\n" + an : "";


            String answerLog = "Termin edit Termin Minutes: " + termin.get().getTerminTime() + " ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editTerminForDays(long chatId, String terminId, String terminMinusdays) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        if (!terminMinusdays.isEmpty()) {
            termin.get().setTerminMinusDay(terminMinusdays);
            terminRepository.save(termin.get());


            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Termin edit Termin days: " + termin.get().getTerminMinusDay() + "\n" +
                    answerTermin(termin.get(), user.get().getLanguage()) :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Вы изменили дни  до термина: " + termin.get().getTerminMinusDay() + "\n" +
                            answerTermin(termin.get(), user.get().getLanguage()) : "";

            String answerLog = "Termin edit Termin days: " + termin.get().getTerminMinusDay() + " ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private static String answerTermin(Termin termin, String language) {

        String answer;
        String createdDate = termin.getCreatedAt().toString();
        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));

        String time = parser.get(1);
        List<String> timeList = List.of(time.split(":"));

        answer = language.toLowerCase().equals("englisch") ?
                "----------------------------" + " \n" +
                        "ID: " + termin.getId() + "\n" +
                        "----------------------------" + " \n" +
                        "Termin name: " + termin.getTerminName() + " | /teditname_" + termin.getId() + "\n" +
                        "Termin date: " + termin.getTerminDate() + " | /teditdate_" + termin.getId() + "\n" +
                        "Termin time: " + termin.getTerminTime() + " | /tedittime_" + termin.getId() + "\n" +
                        "Termin minuts: " + termin.getTerminMinusMin() + " | /teditminusmin_" + termin.getId() + "\n" +
                        "Termin for days: " + termin.getTerminMinusDay() + " | /teditfordays_" + termin.getId() + "\n" +
                        "----------------------------" + " \n" +
                        "created: " + dateList.get(2) + "." + dateList.get(1) + "." + dateList.get(0) + " " +
                        timeList.get(0) + ":" + timeList.get(1) + "\n" +
                        "----------------------------" + " \n" +
                        "Delete this Termin /deletethistermin_" + termin.getId() :
                language.toLowerCase().equals("russian") ?
                        "----------------------------" + " \n" +
                                "ID: " + termin.getId() + "\n" +
                                "----------------------------" + " \n" +
                                "Термин Имя: " + termin.getTerminName() + " | /teditname_" + termin.getId() + "\n" +
                                "Термин дата: " + termin.getTerminDate() + " | /teditdate_" + termin.getId() + "\n" +
                                "Термин Время: " + termin.getTerminTime() + " | /tedittime_" + termin.getId() + "\n" +
                                "Термин Минуты за: " + termin.getTerminMinusMin() + " | /teditminusmin_" + termin.getId() + "\n" +
                                "Термин дни за: " + termin.getTerminMinusDay() + " | /teditfordays_" + termin.getId() + "\n" +
                                "----------------------------" + " \n" +
                                "создан: " + dateList.get(2) + "." + dateList.get(1) + "." + dateList.get(0) + " " +
                                timeList.get(0) + ":" + timeList.get(1) + "\n" +
                                "----------------------------" + " \n" +
                                "Удалить этот Термин /deletethistermin_" + termin.getId() : ""
        ;
        return answer;
    }

    private static String answerBirthday(Birthday birthday, String language) {
        String answer;

        String createdDate = birthday.getCreatedAt().toString();

        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));


        answer = language.toLowerCase().equals("englisch") ?
                "----------------------------" + " \n" +
                        "ID: " + birthday.getId() + "\n" +
                        "----------------------------" + " \n" +
                        "Firstname: " + birthday.getBirthdayFirstName() + " | /beditfirstname_" + birthday.getId() + "\n" +
                        "Lastname: " + birthday.getBirthdayLastName() + " | /beditlastname_" + birthday.getId() + "\n" +
                        "Birthday date: " + birthday.getBirthdayDate() + " | /beditdate_" + birthday.getId() + "\n" +
                        "Birthday for days: " + birthday.getBirthdayMinusDay() + " | /beditfordays_" + birthday.getId() + "\n" +
                        "----------------------------" + " \n" +
                        "created: " + dateList.get(2) + "." + dateList.get(1) + "." + dateList.get(0) + "\n" +
                        "----------------------------" + " \n" +
                        "Delete this Birthday /deletethisbirthday_" + birthday.getId() :
                language.toLowerCase().equals("russian") ?
                        "----------------------------" + " \n" +
                                "ID: " + birthday.getId() + "\n" +
                                "----------------------------" + " \n" +
                                "Имя: " + birthday.getBirthdayFirstName() + " | /beditfirstname_" + birthday.getId() + "\n" +
                                "Фамилия: " + birthday.getBirthdayLastName() + " | /beditlastname_" + birthday.getId() + "\n" +
                                "День рождения: " + birthday.getBirthdayDate() + " | /beditdate_" + birthday.getId() + "\n" +
                                "Напоминае за эти дни: " + birthday.getBirthdayMinusDay() + " | /beditfordays_" + birthday.getId() + "\n" +
                                "----------------------------" + " \n" +
                                "created: " + dateList.get(2) + "." + dateList.get(1) + "." + dateList.get(0) + "\n" +
                                "----------------------------" + " \n" +
                                "Удалить это день Рождения /deletethisbirthday_" + birthday.getId() : ""
        ;
        return answer;
    }

    private static String answerReminder(Reminder reminder) {

        String createdDate = reminder.getCreatedAt().toString();

        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));

        String onOff = reminder.getReminderOnOff() == null || reminder.getReminderOnOff().equalsIgnoreCase("on") || reminder.getReminderOnOff().equalsIgnoreCase("null") ?
                "Remibder is ON | /offrem_" + reminder.getId() : "Remibder is OFF | /onrem_" + reminder.getId();
        String answer;
        answer =
                "----------------------------" + " \n" +
                        "ID : " + reminder.getId() + "\n" +
                        "----------------------------" + " \n" +
                        onOff + "\n" +
                        "----------------------------" + " \n" +
                        "Tiitle: " + reminder.getReminderTittle() + " | /reditname_" + reminder.getId() + "\n" +
                        "Days: " + reminder.getReminderDays() + " | /reditdays_" + reminder.getId() + "\n" +
                        "Minuts: " + reminder.getReminderMinusMin() + " | /reditminutes_" + reminder.getId() + "\n" +
                        "Time: " + reminder.getReminderTime() + " | /redittime_" + reminder.getId() + "\n" +
                        "----------------------------" + " \n" +
                        "Created: " + dateList.get(2) + "." + dateList.get(1) + "." + dateList.get(0) + "\n" +
                        "----------------------------" + " \n" +
                        "Delete this Reminder /deletethisreminder_" + reminder.getId();

        return answer;
    }


    private void deleteThisReminder(long chatId, String reminderId) {
        if (reminderRepository.findById(Long.parseLong(reminderId)).isPresent()) {

            Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(reminderId));

            String answer = "deleted Reminder: " + reminder.get().getId() + " " + reminder.get().getReminderTittle() + " " + reminder.get().getReminderTime();

            reminderRepository.deleteById(Long.parseLong(reminderId));
            buildMessage(chatId, answer);
            log.info(answer);
        }
        clearSelectedcommend();
    }

    private void deleteThisBirthday(long chatId, String birthdayId) {
        if (birthdayRepository.findById(Long.parseLong(birthdayId)).isPresent()) {
            Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));

            String answer = "Deleted Birthday: " + birthday.get().getId() + " " + birthday.get().getBirthdayFirstName() + " " + birthday.get().getBirthdayLastName() + " "
                    + birthday.get().getBirthdayDate();

            birthdayRepository.deleteById(Long.parseLong(birthdayId));
            buildMessage(chatId, answer);
            log.info(answer);

        }
        clearSelectedcommend();
    }

    private void deleteThisTermin(long chatId, String terminId) {
        if (terminRepository.findById(Long.parseLong(terminId)).isPresent()) {
            Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));

            String answer = "Deleted Termin: " + termin.get().getTerminName() + " " + termin.get().getTerminDate() + " " + termin.get().getTerminTime();

            terminRepository.deleteById(Long.parseLong(terminId));
            buildMessage(chatId, answer);
            log.info(answer);
        }
        clearSelectedcommend();
    }

    private void deleteTermin(Message message, Long chatId) {

        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        for (String termStrId : parser) {
            Long terminId = Long.valueOf(termStrId);


            Optional<Termin> termin = terminRepository.findById(terminId);

            String answer = "deleted termin: " + termin.get().getId() + " " + termin.get().getTerminName();

            terminRepository.deleteById(terminId);
            buildMessage(chatId, answer);
            log.info(answer);
            selectedCommands.clear();
        }
    }

    private void deleteOneReminder(Message message, long chatId) {
        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        for (String remStrId : parser) {

            Long reminderId = Long.valueOf(remStrId);

            Optional<Reminder> reminder = reminderRepository.findById(reminderId);

            String answer = "deleted Reminder: " + reminder.get().getId() + " " + reminder.get().getReminderTittle() + " " + reminder.get().getReminderTime();

            reminderRepository.deleteById(reminderId);
            buildMessage(chatId, answer);
            log.info(answer);
            selectedCommands.clear();
        }
    }

    private void deleteBirthday(Message message, long chatId) {
        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        for (String birdStrId : parser) {
            Long birthdayId = Long.valueOf(birdStrId);

            Optional<Birthday> birthday = birthdayRepository.findById(birthdayId);

            String answer = "deleted Birthday: " + birthday.get().getId() + " " + birthday.get().getBirthdayFirstName() + " " + birthday.get().getBirthdayLastName();

            birthdayRepository.deleteById(birthdayId);
            buildMessage(chatId, answer);
            log.info(answer);
            selectedCommands.clear();
        }
    }

    // TODO: 12.01.2024 mydata
    //UserFunk
    private void myDataUser(long chatId) {
        String answer = "";
        Optional<User> user;
        user = Optional.of(userRepository.findById(chatId).get());

        if (userRepository.findById(chatId).isPresent()) {
            if (user.get().getLanguage().toLowerCase().equals("englisch")) {
                answer = engMyDataTex(chatId);
            } else if (user.get().getLanguage().toLowerCase().equals("russian")) {
                answer = ruMyDataTex(chatId);
            }

        } else {
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "you are not in our database yet,\n" +
                    "if you want to register, then click /registrade " : user.get().getLanguage().toLowerCase().equals("russian") ? "Вы не зарегестрированные\n" +
                    "чтобы зарегестрироваться, нажмите /registrade" : "";
        }
        buildMessage(chatId, answer);
    }

    private String engMyDataTex(long chatId) {
        Optional<User> user;
        user = Optional.of(userRepository.findById(chatId).get());
        List<Termin> termins = terminRepository.findByUserId(chatId);
        List<Birthday> birthdays = birthdayRepository.findByUserId(chatId);
        List<Reminder> reminders = reminderRepository.findByUserId(chatId);
        String answer;


        String id = String.valueOf(user.get().getId());
        String userNameInfo = user.get().getUserName() == null ? "your username null, please set you /username" :
                user.get().getUserName().equalsIgnoreCase("null") ? "your username null, please set you /username" :
                        "user: " + user.get().getUserName() + " | Edit /username";
        String firstNameInfo = user.get().getFirstName() == null ? "your Firstname null, please set you /firstname" :
                user.get().getFirstName().equalsIgnoreCase("null") ? "your Firstname null, please set you /firstname" :
                        "Firstname: " + user.get().getFirstName() + " | Edit /firstname";
        String lastNameInfo = user.get().getLastName() == null ? "your lastname null, please set you /lastname" :
                user.get().getLastName().equalsIgnoreCase("null") ? "your lastname null, please set you /lastname" :
                        "Lastname: " + user.get().getLastName() + " | Edit /lastname";


        String terminInfo = !termins.isEmpty() ? "You have saved  Termins: " + termins.size() + " \n" + "show all Termins /showtermin \n" :
                "You have not Termins, ADD  /newtermin \n";
        String birthdayInfo = !birthdays.isEmpty() ? "You have saved  Birthdays: " + birthdays.size() + " \n" + "show all Birthdays /showbirthday " + "\n" :
                "You have not Birthdays, ADD  /newbirthday \n";
        String reminderInfo = !reminders.isEmpty() ? "You have saved  Reminders: " + reminders.size() + " \n" + "show all Reminders /showreminder " + "\n" :
                "You have not Reminders, ADD  /newreminder \n";

        String birthdayRemindForDays = user.get().getBirthdayFordaysTime() != null ?
                "Birthday reminders for day will come at: " + user.get().getBirthdayFordaysTime() + " | Edit /beditforeverydaystime" :
                "Birthday reminders for day  will come at: 18:00 | /beditforeverydaystime";
        String birthdayRemindNowDays = user.get().getBirthdayNowDayTime() != null ?
                "Birthday reminders for this day will come at: " + user.get().getBirthdayNowDayTime() + "| Edit /beditfornowdaytime" :
                "Birthday reminders for this day will come at: 18:00 | Edit /beditfornowdaytime";
        String terminRemindForDays = user.get().getTerminFordaysTime() != null ?
                "Termin reminders will come at: " + user.get().getTerminFordaysTime() + " | Edit /teditforeverydaystime" :
                "Termin reminders will come at: 18:00 | Edit /teditforeverydaystime";

        LocalTime localTime = localTimeNow(chatId);


        answer = "this is your saved data\n" +
                "----------------------------" + " \n" +
                id + " \n" +
                userNameInfo + " \n" +
                firstNameInfo + " \n" +
                lastNameInfo + " \n" +

                "----------------------------" + " \n" +
                "You Time: " + dtf.format(localTime) + " \n" +

                "Region: " + user.get().getRegion() + "\n set you /setregion for your correctly Time" + "\n" +
                "Language: " + user.get().getLanguage() + "\n set you /setlanguage for your menu language" + "\n" +
                "----------------------------" + " \n" +
                "registered: " + user.get().getRegisteredAt() + "\n" +
                "----------------------------" + " \n" +
                terminInfo + birthdayInfo + reminderInfo +
                "-------------SETTING---------------" + " \n" +
                terminRemindForDays + " \n" +
                birthdayRemindForDays + " \n" +
                birthdayRemindNowDays + " \n" +
                "----------------------------" + " \n" +
                "\n" +
                "if you want to delete click /deleteuserdata";

        log.info("Show UserData: " + user.get().getUserName()
                + ", " + user.get().getFirstName()
                + ", " + user.get().getLastName()
                + ", Region: " + user.get().getRegion()
                + ", Registered: " + user.get().getRegisteredAt()
                + ", User have saved Termins: " + termins.size() + "\n"
                + ", User have saved Birthdays: " + birthdays.size() + "\n"
                + ", User have saved Reminders: " + reminders.size() + "\n"
        );
        return answer;
    }

    private String ruMyDataTex(long chatId) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        List<Termin> termins = terminRepository.findByUserId(chatId);
        List<Birthday> birthdays = birthdayRepository.findByUserId(chatId);
        List<Reminder> reminders = reminderRepository.findByUserId(chatId);
        String answer;


        String id = String.valueOf(user.get().getId());
        String userNameInfo = user.get().getUserName() == null ? "Твой ник null, чтобы изменить /username" :
                user.get().getUserName().equalsIgnoreCase("null") ? "Твой ник null, чтобы изменить /username" :
                        "Ник: " + user.get().getUserName() + " | изменить /username";

        String firstNameInfo = user.get().getFirstName() == null ? "Твоё имя null, чтобы изменить /firstname" :
                user.get().getFirstName().equalsIgnoreCase("null") ? "Твоё имя null, чтобы изменить /firstname" :
                        "Имя: " + user.get().getFirstName() + " | изменить /firstname";

        String lastNameInfo = user.get().getLastName() == null ? "Твоя Фамилия null, чтобы изменить /lastname" :
                user.get().getLastName().equalsIgnoreCase("null") ? "Твоя Фамилия null, чтобы изменить /lastname" :
                        "Фамилия: " + user.get().getLastName() + " | изменить /lastname";


        String terminInfo = !termins.isEmpty() ? "У тебя сохраненно " + termins.size() + " Терминов\n" + "показать все Термины /showtermin \n" :
                "У тебя нет ни одного Термина, добавить /newtermin \n";
        String birthdayInfo = !birthdays.isEmpty() ? "У тебя сохраненно " + birthdays.size() + " Дней Рождений\n" + "показать все Дни Рождении /showbirthday " + "\n" :
                "У тебя нет ни одного Дня Рождения, добавить /newbirthday \n";
        String reminderInfo = !reminders.isEmpty() ? "У тебя сохраненно " + reminders.size() + " Напоминаний\n" + "показать все Напоминая /showreminder " + "\n" :
                "У тебя нет ни одного Напоминая, добавить  /newreminder \n";

        String birthdayRemindForDays = user.get().getBirthdayFordaysTime() != null ?
                "Время когда будет приходит Напоминание о дне рождение до его дня: " + user.get().getBirthdayFordaysTime() + " | изменить /beditforeverydaystime" :
                "Время когда будет приходит Напоминание о дне рождение до его дня: 18:00 | изменить /beditforeverydaystime";
        String birthdayRemindNowDays = user.get().getBirthdayNowDayTime() != null ?
                "Время когда будет приходит Напоминание о дне рождение в этот день: " + user.get().getBirthdayNowDayTime() + "| изменить /beditfornowdaytime" :
                "Время когда будет приходит Напоминание о дне рождение в этот день: 18:00 | изменить /beditfornowdaytime";
        String terminRemindForDays = user.get().getTerminFordaysTime() != null ?
                "Время когда будет приходит Напоминание о термине до его дня: " + user.get().getTerminFordaysTime() + " | изменить /teditforeverydaystime" :
                "Время когда будет приходит Напоминание о термине до его дня: 18:00 | изменить /teditforeverydaystime";

        LocalTime localTime = localTimeNow(chatId);


        answer = "Это твои Данные\n" +
                "----------------------------" + " \n" +
                id + " \n" +
                userNameInfo + " \n" +
                firstNameInfo + " \n" +
                lastNameInfo + " \n" +

                "----------------------------" + " \n" +
                "твоё время: " + dtf.format(localTime) + " \n" +

                "Регион: " + user.get().getRegion() + "\n измини свой /setregion для правильного отсылки напоминаний" + "\n" +
                "Язык: " + user.get().getLanguage() + "\n измини язык /setlanguage для отоброжения в меню" + "\n" +
                "----------------------------" + " \n" +
                "создал аккаунт: " + user.get().getRegisteredAt() + "\n" +
                "----------------------------" + " \n" +
                terminInfo + birthdayInfo + reminderInfo +
                "-------------Настройки---------------" + " \n" +
                terminRemindForDays + " \n" +
                birthdayRemindForDays + " \n" +
                birthdayRemindNowDays + " \n" +
                "----------------------------" + " \n" +
                "\n" +
                "если хочешь удалить все данные /deleteuserdata";

        log.info("Show UserData: " + user.get().getUserName()
                + ", " + user.get().getFirstName()
                + ", " + user.get().getLastName()
                + ", Region: " + user.get().getRegion()
                + ", Registered: " + user.get().getRegisteredAt()
                + ", User have saved Termins: " + termins.size() + "\n"
                + ", User have saved Birthdays: " + birthdays.size() + "\n"
                + ", User have saved Reminders: " + reminders.size() + "\n"
        );
        return answer;
    }

    private void deleteUserData(Long chatId, String messageText) {

        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
        if (containsDigitsRegex(messageText)) {
            long userEnterId = Long.parseLong(messageText);


            Long userId = user.get().getId();

            String idLenght = userId.toString();


            String userIdEit = userId.toString().substring(idLenght.length() - 4, idLenght.length());

            long userIdCheck = Long.parseLong(userIdEit);

            if (userIdCheck == userEnterId) {
                deleteUser(chatId);
            } else {
                buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "you entered the wrong numbers" : user.get().getLanguage().toLowerCase().equals("russian") ? "Вы ввели не правильный номмер" : "");

            }
        } else if (!containsDigitsRegex(messageText)) {
            buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "you entered the wrong numbers" : user.get().getLanguage().toLowerCase().equals("russian") ? "Вы ввели не правильный номмер" : "");

        } else {
            buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "you entered the wrong numbers" : user.get().getLanguage().toLowerCase().equals("russian") ? "Вы ввели не правильный номмер" : "");

        }
        selectedCommands.clear();

    }

    private static boolean containsDigitsRegex(String input) {
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private void deleteUser(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            log.info("Delete User: " + user.get().getUserName()
                    + ", " + user.get().getFirstName()
                    + ", " + user.get().getLastName()
                    + ", Registered: " + user.get().getRegisteredAt());
            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "your data has been deleted from the database " + user.get().getUserName() :
                    user.get().getLanguage().toLowerCase().equals("russian") ?"Вы удалили все ваши данные " + user.get().getUserName():"";
            userRepository.deleteById(chatId);
            buildMessage(chatId, answer);
        }
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = User.builder()
                    .id(chatId)
                    .firstName(wordFirstCharToLower(chat.getFirstName()))
                    .lastName(wordFirstCharToLower(chat.getLastName()))
                    .userName(chat.getUserName())
                    .language("Englisch")
                    .region("null")
                    .terminFordaysTime("18:00")
                    .birthdayFordaysTime("18:00")
                    .birthdayNowDayTime("18:00")
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("User save: " + user);
            String answer = user.getLanguage().toLowerCase().equals("englisch") ? "User save: " + user.getUserName()
                    : user.getLanguage().toLowerCase().equals("russian") ? "Пользовотель сохранён: " + user.getUserName():"";
            buildMessage(chatId, answer);
        }
    }

    private void setUsername(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String userName = message.getText();
        if (!userName.isEmpty()) {
            user.get().setUserName(userName);
            userRepository.save(user.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set Username: " + userName :
                    user.get().getLanguage().toLowerCase().equals("russian") ?"Пользователь: " + user.get().getUserName() + " изменил ник  пользователя: " + userName :"";
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();
    }

    private void setLanguage(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String language = message.getText();
        if (!language.isEmpty()) {
            user.get().setLanguage(language);
            userRepository.save(user.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set language: " + language
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользователь: " + user.get().getUserName() + " изменил Язык: " + language:"";
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();
    }

    private void setfirstname(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String firstName = message.getText();
        if (!firstName.isEmpty()) {
            user.get().setFirstName(wordFirstCharToLower(firstName));
            userRepository.save(user.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set Firstname: " + firstName
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользовотель: " + user.get().getUserName() + " изменил Имя: " + firstName:"";
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();
    }

    private void setLastname(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String lastName = message.getText();
        if (!lastName.isEmpty()) {
            user.get().setLastName(wordFirstCharToLower(lastName));
            userRepository.save(user.get());

            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set Lastname: " + lastName
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользовотель: " + user.get().getUserName() + " изменил Фамилию: " + lastName:"";;
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();
    }

    private void setTimeForEveryDayBirthday(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String checkTimes = message.getText();
        String answer = null;
        String answerLog = null;
        String saveTime = null;
        boolean checkTimeSettings = checkTimeSettingsFnk(chatId, checkTimes);

        if (checkTimeSettings) {
            saveTime = checkTimes;
            user.get().setBirthdayFordaysTime(saveTime);
            userRepository.save(user.get());
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользователь: " + user.get().getUserName() + " изменил время когда будет приходить оповешение о дне рождения до его дня: " + saveTime:"";


            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setBirthdayFordaysTime(saveTime);
            userRepository.save(user.get());

            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User set false time | Edit /beditforeverydaystime "
                    : user.get().getLanguage().toLowerCase().equals("russian") ?"Пользователь ввёл не коректное время | изьенить /beditforeverydaystime":"";
            answerLog = "User set false time";

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }
    private void setTimeForNowDayBirthday(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String checkTimes = message.getText();
        String answer = null;
        String answerLog = null;
        String saveTime = null;
        boolean checkTimeSettings = checkTimeSettingsFnk(chatId, checkTimes);

        if (checkTimeSettings) {
            saveTime = checkTimes;
            user.get().setBirthdayNowDayTime(saveTime);
            userRepository.save(user.get());
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользователь: " + user.get().getUserName() + " изменил время когда будет приходить оповешение о дне рождения в этот день: " + saveTime:"";
            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setBirthdayNowDayTime(saveTime);
            userRepository.save(user.get());

            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User set false time | Edit /beditfornowdaytime "
                    : user.get().getLanguage().toLowerCase().equals("russian") ?"Пользователь ввёл не коректное время | изьенить /beditfornowdaytime":"";
            answerLog = "User set false time";

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }
    private void setTimeTerminForEveryDays(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String checkTimes = message.getText();
        String answer = null;
        String answerLog = null;
        String saveTime = null;
        boolean checkTimeSettings = checkTimeSettingsFnk(chatId, checkTimes);

        if (checkTimeSettings) {
            saveTime = checkTimes;
            user.get().setTerminFordaysTime(saveTime);
            userRepository.save(user.get());
            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime
                    : user.get().getLanguage().toLowerCase().equals("russian") ? "Пользователь: " + user.get().getUserName() + " изменил время когда будет приходить оповешение о Терминах до его дня: " + saveTime:"";
            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setTerminFordaysTime(saveTime);
            userRepository.save(user.get());

            answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User set false time | Edit /teditforeverydaystime"
                    : user.get().getLanguage().toLowerCase().equals("russian") ?"Пользователь ввёл не коректное время | изьенить /teditforeverydaystime":"";
            answerLog = "User set false time";

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();


    }
    private boolean checkTimeSettingsFnk(long chatId, String checkTimes) {
        List<String> timesList = new ArrayList<>();

        byte trueCount = 0;
        boolean res = false;

        if (checkTimes.contains(", ")) {
            timesList = List.of(checkTimes.split(", "));
        } else if (checkTimes.contains(" ")) {
            timesList = List.of(checkTimes.split(" "));
        } else if (!" ".contains(checkTimes)) {
            timesList.add(checkTimes);
        }

        if (!timesList.isEmpty()) {
            for (String checkTime : timesList) {
                String checkTimeAll = timeCheckFnk(chatId, checkTime);
                List<String> timeTrueFalse = List.of(checkTimeAll.split("&"));
                List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));

                if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {
                    trueCount++;
                }
            }


        }
        res = timesList.size() == trueCount;

        return res;
    }
    private void setUserRegion(Message region, long chatId) {
        Optional<User> user = userRepository.findById(chatId);
        String regionCheck = region.getText();

        Map<String, Integer> regionsMap = getRegionsMap();


        if (!regionCheck.isEmpty()) {
            String regionsFirstToLower = wordFirstCharToLower(regionCheck);


            if (regionsMap.containsKey(regionsFirstToLower)) {

                user.get().setRegion(regionsFirstToLower);
                userRepository.save(user.get());

                String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set region: " + regionsFirstToLower
                        : user.get().getLanguage().toLowerCase().equals("russian") ? "Поользователь: " + user.get().getUserName() + " изменил регион: " + regionsFirstToLower :"";
                buildMessage(chatId, answer);
                log.info(answer);
                myDataUser(chatId);
            } else {
                String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "User: " + user.get().getUserName() + " set false region: " + regionsFirstToLower + "\n" +
                        regionsMapToString(regionsMap) + "\n" +
                        "/setregion"
                        : user.get().getLanguage().toLowerCase().equals("russian") ? "Поользователь: " + user.get().getUserName() + " ввёл не правильный регион: " + regionsFirstToLower + "\n" +
                                                regionsMapToString(regionsMap) + "\n" +
                                                "/setregion":"";

                buildMessage(chatId, answer);
                log.info(answer);

            }

        }
        clearSelectedcommend();

    }

    private String wordFirstCharToLower(String checkWord) {
        if (checkWord == null || checkWord.isEmpty()) {

            checkWord = "null";
            return checkWord;
        }

        boolean b = !checkWord.isEmpty() && Character.isUpperCase(checkWord.charAt(0));
        if (b) {
            return checkWord;
        } else {
            return Character.toUpperCase(checkWord.charAt(0)) + checkWord.substring(1);
        }
    }

    private static String regionsMapToString(Map<String, Integer> regionsMap) {
        List<String> regionList = new ArrayList<>();
        for (Map.Entry<String, Integer> regionEntry : regionsMap.entrySet()) {
            regionList.add(regionEntry.getKey());
        }
        return String.join("\n", regionList);
    }

    //StartFunk
    private void help(long chatId) {
        log.info("Replied to user: ");
        buildMessage(chatId, HELP_TEXT_START_ENGL);
    }

    private void start(long chatId) {

        Optional<User> user = userRepository.findById(chatId);

        String userName = user.get().getUserName();
        String setRegion = user.get().getRegion().equalsIgnoreCase("null") ?
                user.get().getLanguage().toLowerCase().equals("englisch") ? "Please set you Region for correctly time! /setregion" :
                        user.get().getLanguage().toLowerCase().equals("russian") ? "Пожалуйста установите ваш регион для правильного работы приложения! /setregion" :"" : "";

        String answer = user.get().getLanguage().toLowerCase().equals("englisch") ?
                user.get().getUserName().equalsIgnoreCase("null") ? "HI, your username is null, please set you /username" + setRegion :
                "Hi. " + userName + " nice to meet you.\n" + setRegion
                : user.get().getLanguage().toLowerCase().equals("russian") ?
                user.get().getUserName().equalsIgnoreCase("null") ? "Привет, твой ник пуст, пожалуйста отредактируй его нажав /username" + setRegion :
                "Привет. " + userName + " приятно познакомиться.\n" + setRegion
        :"";

        log.info("Replied to user: " + userName);
        buildMessage(chatId, answer);
    }

    private void defaultComand(long chatId, String userName, String messageText) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Sorry, command was not recognized"
                : user.get().getLanguage().toLowerCase().equals("russian") ?"Извините такой команды нет":"";

        log.info("Replied to user: " + userName + " user falls command " + messageText);
        buildMessage(chatId, answer);
    }

    @Scheduled(cron = "${she.reminder}")
    private void reminderSender() {
        List<Reminder> allReminders = reminderRepository.findAll();
        List<Integer> reminderDays = new ArrayList<>();

        if (!allReminders.isEmpty()) {
            for (Reminder reminder : allReminders) {

                LocalDate localDate = LocalDate.now();

                List<String> days = List.of(reminder.getReminderDays().split(" "));
                for (String day : days) {
                    reminderDays.add(Integer.parseInt(day));
                }

                List<String> timeParse = List.of(reminder.getReminderTime().split(":"));

                List<String> minusMinSort;
                if (reminder.getReminderMinusMin() != null) {
                    minusMinSort = sortedList(reminder.getReminderMinusMin());
                } else {
                    minusMinSort = sortedList("05 10 15");
                }


                LocalTime reminderTime = LocalTime.of(Integer.parseInt(timeParse.get(0)), Integer.parseInt(timeParse.get(1)));

                int weekDayNow = localDate.getDayOfWeek().getValue();

                sendReminder(reminder, minusMinSort, reminderTime, reminderDays, weekDayNow);

                reminderDays.clear();

            }
        }
    }

    @Scheduled(cron = "${she.termin}")
    private void terminSender() {

        List<Termin> allTermins = terminRepository.findAll();
        if (!allTermins.isEmpty()) {
            for (Termin termin : allTermins) {

                LocalDate localDate = LocalDate.now();


                List<String> dateParse = List.of(termin.getTerminDate().split("\\."));
                List<String> timeParse = List.of(termin.getTerminTime().split(":"));

                LocalDate terminDate = LocalDate.of(Integer.parseInt(dateParse.get(2)), Integer.parseInt(dateParse.get(1)), Integer.parseInt(dateParse.get(0)));
                LocalTime terminTime = LocalTime.of(Integer.parseInt(timeParse.get(0)), Integer.parseInt(timeParse.get(1)));

                List<String> minusMinSort;
                if (termin.getTerminMinusMin() != null) {
                    minusMinSort = sortedList(termin.getTerminMinusMin());
                } else {
                    minusMinSort = sortedList("05 10 15");
                }
                sendTermin(termin, minusMinSort, terminTime, localDate, terminDate);
            }
        }
    }

    @Scheduled(cron = ("${she.terminD}"))
    private void terminSenderDays() {
        List<Termin> allTermins = terminRepository.findAll();
        LocalDate expectedDay;
        LocalDate localDate = LocalDate.now();
        String terminForDaysTime;
        if (!allTermins.isEmpty()) {
            for (Termin termin : allTermins) {
                Optional<User> user = userRepository.findById(termin.getUser().getId());
                terminForDaysTime = user.get().getTerminFordaysTime() != null ?
                        user.get().getTerminFordaysTime() :
                        "18:00";

                List<String> splitTime = checkParseTime(user.get().getId(), terminForDaysTime);

                for (String time : splitTime) {

                    String timeTrueFalse = timeCheckFnk(user.get().getId(), time);
                    String[] splitTrueFalse = timeTrueFalse.split("&");

                    String timeChecked = splitTrueFalse[0];

                    String[] timeSplit = timeChecked.split(":");
                    LocalTime forDaysTime = LocalTime.of(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]));

                    List<String> dateParse = List.of(termin.getTerminDate().split("\\."));
                    LocalDate terminDate = LocalDate.of(Integer.parseInt(dateParse.get(2)), Integer.parseInt(dateParse.get(1)), Integer.parseInt(dateParse.get(0)));

                    String terminMinusDayExpected = termin.getTerminMinusDay();

                    expectedDay = terminDate;

                    String forDay;

                    String terminMinusDay;
                    if (terminMinusDayExpected == null || terminMinusDayExpected.isEmpty() || terminMinusDayExpected.equals("null")) {
                        terminMinusDay = "1 3 5";
                    } else {
                        terminMinusDay = terminMinusDayExpected;
                    }


                    if (!terminMinusDay.equals("0")) {
                        List<String> terminsMinus = sortedList(terminMinusDay);

                        for (String dayString : terminsMinus) {
                            expectedDay = terminDate.minusDays(Long.parseLong(dayString));
                            forDay = dayString;

                            if (Period.between(localDate, expectedDay).getMonths() == 0
                                    && Period.between(localDate, expectedDay).getDays() == 0
                            ) {
                                Long chatId = termin.getUser().getId();
                                String answer = "In " + forDay + " day Have you Termin\n" + termin.getTerminName() + "\nDate: " + termin.getTerminDate() + "\nTime: " + termin.getTerminTime();
                                messageStack.add(MessageToSend.builder()
                                        .chatId(chatId)
                                        .messageTyp("Termin")
                                        .messageToSend(answer)
                                        .messageToLog("")
                                        .time(regionsTime(chatId, forDaysTime))
                                        .build());


                            }
                        }
                    }
                }
            }
        }
    }

    @Scheduled(cron = ("${she.birthday}"))
    private void buildBirthdayScheduler() {
        List<Birthday> allbirthdays = birthdayRepository.findAll();
        LocalDate expectedDay;
        LocalDate localDate = LocalDate.now();
        String birthdayForDaysTime;
        List<String> parseTime;

        if (!allbirthdays.isEmpty()) {
            String birthdayMinusDay = null;
            for (Birthday birthday : allbirthdays) {
                Optional<User> user = userRepository.findById(birthday.getUser().getId());
                birthdayForDaysTime = user.get().getBirthdayFordaysTime() != null ?
                        user.get().getBirthdayFordaysTime() : "18:00";

                parseTime = checkParseTime(user.get().getId(), birthdayForDaysTime);

                for (String time : parseTime) {

                    String timeTrueFalse = timeCheckFnk(user.get().getId(), time);
                    String[] splitTrueFalse = timeTrueFalse.split("&");

                    String timeChecked = splitTrueFalse[0];

                    String[] timeSplit = timeChecked.split(":");
                    LocalTime forDaysTime = LocalTime.of(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]));

                    List<String> dateParse = List.of(birthday.getBirthdayDate().split("\\."));
                    LocalDate birthdayDate = LocalDate.of(Integer.parseInt(dateParse.get(2)), Integer.parseInt(dateParse.get(1)), Integer.parseInt(dateParse.get(0)));

                    String birthdayMinusDayExpected = birthday.getBirthdayMinusDay();
                    expectedDay = birthdayDate;
                    String forDay;

                    if (!birthday.getBirthdayMinusDay().isEmpty()) {

                        if (birthdayMinusDayExpected.equals("null")) {
                            birthdayMinusDay = "1 3 5";
                        } else {
                            birthdayMinusDay = birthdayMinusDayExpected;
                        }

                        if (birthdayMinusDay.equals("0")) {
                            nowBirthdaySend(expectedDay, birthday, localDate);
                        } else {
                            List<String> birthdaysMinus = sortedList(birthdayMinusDay);
                            for (String dayString : birthdaysMinus) {
                                expectedDay = birthdayDate.minusDays(Long.parseLong(dayString));
                                forDay = dayString;

                                if (Period.between(localDate, expectedDay).getMonths() == 0
                                        && Period.between(localDate, expectedDay).getDays() == 0
                                ) {
                                    Long chatId = birthday.getUser().getId();
                                    String answer = "In " + forDay + " day " + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLastName() + "  have birthday!! | Date: " + birthday.getBirthdayDate();
                                    messageStack.add(MessageToSend.builder()
                                            .chatId(chatId)
                                            .messageTyp("Birthday")
                                            .messageToSend(answer)
                                            .messageToLog("")
                                            .time(regionsTime(chatId, forDaysTime))
                                            .build());

                                }
                            }
                        }

                        if (Period.between(localDate, birthdayDate).getMonths() == 0
                                && Period.between(localDate, birthdayDate).getDays() == 0) {
                            nowBirthdaySend(birthdayDate, birthday, localDate);

                        }
                    }
                }
            }
        }
    }

    @Scheduled(cron = ("${she.stack}"))
    @Async
    protected void messageStackSort() {
        if (!messageStack.isEmpty()) {
//TODO: доделать стэк
            List<MessageToSend> sortedTime = messageStack.stream()
                    .sorted(Comparator.comparing(MessageToSend::getTime))
                    .filter(a -> ChronoUnit.MINUTES.between(LocalTime.now().minusMinutes(5), a.getTime()) >= 0)
                    .filter(a -> ChronoUnit.MINUTES.between(LocalTime.now().plusHours(1), a.getTime()) <= 0)
                    .toList();

            for (MessageToSend messageSend : sortedTime) {
                //System.out.println("ID: " + messageSend.getChatId()/*+" MSG "+message.getMessageToSend()*/ + " Typ " + messageSend.getMessageTyp() + " time " + messageSend.getTime());


                log.info("ChatID: " + messageSend.getChatId() +
                        " MSG " + messageSend.getMessageToSend() +
                        " time " + messageSend.getTime());

                if (
                        ChronoUnit.HOURS.between(LocalTime.now(), messageSend.getTime()) == 0
                                && ChronoUnit.MINUTES.between(LocalTime.now(), messageSend.getTime()) == 0
                                && ChronoUnit.SECONDS.between(LocalTime.now(), messageSend.getTime()) == 0
                ) {
                    buildMessage(messageSend.getChatId(), messageSend.getMessageToSend());
                    log.info("Message send: " + messageSend.getMessageToLog());

                }
            }
            messageStack.clear();
        }
    }

    private void sendReminder(Reminder reminder, List<String> minusMinSort, LocalTime reminderTime, List<Integer> reminderDays, int weekDayNow) {

        int count = 0;
        LocalTime reminderTimeMin;
        for (String minuts : minusMinSort) {
            reminderTimeMin = minuts.equals("0") ? reminderTime : reminderTime.minusMinutes(Integer.parseInt(minuts));

            if (
                    reminder.getReminderOnOff() == null
                            || reminder.getReminderOnOff().equalsIgnoreCase("on")
                            || reminder.getReminderOnOff().equalsIgnoreCase("null")) {

                if (reminderDays.contains(weekDayNow)) {
                    Long chatId = reminder.getUser().getId();
                    Optional<User> user = Optional.of(userRepository.findById(chatId).get());


                    if (!minusMinSort.get(0).equals("0")) {
                        String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "In " + minuts + " minutes it will be | " + reminder.getReminderTittle() + " | at " + reminder.getReminderTime() + " o'clock." :
                                user.get().getLanguage().toLowerCase().equals("russian") ? "Через " + minuts + " мин. будет | " + reminder.getReminderTittle() + " | в " + reminder.getReminderTime():"";
                        messageStack.add(MessageToSend.builder()
                                .chatId(chatId)
                                .messageTyp("Reminder")
                                .messageToSend(answer)
                                .messageToLog("")
                                .time(regionsTime(chatId, reminderTimeMin))
                                .build());
                    }
                }
                count++;


                if (
                        minusMinSort.isEmpty()
                                || minusMinSort.get(0).equals("0")
                                || minusMinSort.size() == count) {
                    if (reminderDays.contains(weekDayNow)) {
                        Long chatId = reminder.getUser().getId();
                        Optional<User> user = Optional.of(userRepository.findById(chatId).get());
                        String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "Now " + reminder.getReminderTittle() + " " + reminder.getReminderTime()
                                : user.get().getLanguage().toLowerCase().equals("russian") ? "Сейчас " + reminder.getReminderTittle() + " " + reminder.getReminderTime():"";
                        messageStack.add(MessageToSend.builder()
                                .chatId(chatId)
                                .messageTyp("Reminder")
                                .messageToSend(answer)
                                .messageToLog(answer)
                                .time(regionsTime(chatId, reminderTime))
                                .build());

                    }
                }
            }
        }
    }

    private void sendTermin(Termin termin, List<String> minusMinSort, LocalTime terminTime, LocalDate localDate, LocalDate terminDate) {
        int count = 0;

        for (String s : minusMinSort) {

            LocalTime terminTimeMin = s.equals("0") ? terminTime : terminTime.minusMinutes(Integer.parseInt(
                    s));

            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0


            ) {
                Long chatId = termin.getUser().getId();
                Optional<User> user = Optional.of(userRepository.findById(chatId).get());
                String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? termin.getTerminName() + "\nIn " + s + " minutes it will be" + "\nat " + termin.getTerminTime() + " o'clock\nDate: " + termin.getTerminDate()
                        : user.get().getLanguage().toLowerCase().equals("russian") ? termin.getTerminName() + "\nЧерез " + s + " мин. в \n " + termin.getTerminTime() + "\nДата: " + termin.getTerminDate() :"";
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Termin")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(regionsTime(chatId, terminTimeMin))
                        .build());
            }
            count++;
        }

        if (minusMinSort.isEmpty() || minusMinSort.get(0).equals("0") || minusMinSort.size() == count) {
            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0
            ) {
                Long chatId = termin.getUser().getId();
                Optional<User> user = Optional.of(userRepository.findById(chatId).get());
                String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? termin.getTerminName() + "\nNow \n" + "at " + termin.getTerminTime() + " o'clock\nDate: " + termin.getTerminDate() :
                        user.get().getLanguage().toLowerCase().equals("russian") ? termin.getTerminName() + "\nСейчас \n" + "в " + termin.getTerminTime() + " времени\nДата: " + termin.getTerminDate() :"";
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Termin")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(regionsTime(chatId, terminTime))
                        .build());
            }
        }
    }

    private void nowBirthdaySend(LocalDate expectedDay, Birthday birthday, LocalDate localDate) {
        Optional<User> user = userRepository.findById(birthday.getUser().getId());

        String birthdayNowDayTime = user.get().getBirthdayNowDayTime() != null ?
                user.get().getBirthdayNowDayTime() : "13:00 18:00";

        List<String> splitTime = checkParseTime(user.get().getId(), birthdayNowDayTime);

        for (String time : splitTime) {

            String[] timeSplit = time.split(":");
            LocalTime nowDayTime = LocalTime.of(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]));

            if (Period.between(localDate, expectedDay).getMonths() == 0
                    && Period.between(localDate, expectedDay).getDays() == 0
            ) {
                Long chatId = birthday.getUser().getId();
                String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "today " + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLastName() + " birthday |  " + birthday.getBirthdayDate()
                        : user.get().getLanguage().toLowerCase().equals("russian") ? "Cегодня у " + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLastName() + " денб рождения |  " + birthday.getBirthdayDate():"";

                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Birthday")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(regionsTime(chatId, nowDayTime))
                        .build());
            }
        }
    }

    private List<String> checkParseTime(Long chatId, String times) {
        List<String> splitTimes = new ArrayList<>();

        List<String> splitTimeRes = new ArrayList<>();

        if (times.contains(" ")) {
            splitTimes = List.of(times.split(" "));
        } else if (times.contains(", ")) {
            splitTimes = List.of(times.split(", "));
        } else if (!times.isEmpty()) {
            splitTimes.add(times);
        }

        for (String expectedTime : splitTimes) {
            String timeBoolean = timeCheckFnk(chatId, expectedTime);

            String[] timeBooleanTrim = timeBoolean.split("&");


            splitTimeRes.add(timeBooleanTrim[0]);
        }
        return splitTimeRes;
    }

    private static List<String> sortedList(String sort) {
        List<String> minusMinEdit = new ArrayList<>();
        List<String> minusMinEditTrim = new ArrayList<>();
        List<String> minusMinEditAddNull = new ArrayList<>();


        if (sort.contains(" ")) {
            minusMinEdit = List.of(sort.split(" "));

        }
        if (sort.contains(", ")) {
            minusMinEdit = List.of(sort.split(", "));
        }
        if (sort.contains(",")) {
            minusMinEdit = List.of(sort.split(","));
        }
        if (sort.contains(".")) {
            minusMinEdit = List.of(sort.split("\\."));
        }
        if (sort.length() == 1) {
            minusMinEdit = List.of(sort);
        }

        for (String s : minusMinEdit) {
            minusMinEditTrim.add(s.trim());
        }

        for (String s : minusMinEditTrim) {
            if (s.length() == 1) {
                minusMinEditAddNull.add("0" + s);
            } else {
                minusMinEditAddNull.add(s);
            }
        }
        List<String> minusMinSortEdit = minusMinEditAddNull.stream().sorted(Comparator.reverseOrder()).toList();

        List<String> minusMinSort = new ArrayList<>();

        for (String s : minusMinSortEdit) {

            if (s.length() >= 2) {
                String x = s.substring(0, 1);
                if (x.equals("0")) {
                    minusMinSort.add(s.substring(1));
                } else {
                    minusMinSort.add(s);
                }
            } else if (s.length() == 1) {
                minusMinSort.add(s);
            }
        }


        return minusMinSort;
    }

    private String dateCheckFnk(Long chatId, String dateCheck) {
        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        String dayCheck;
        String monthCheck;
        String yearCheck;
        String spliterCheck;

        String dayBoolean;
        String monthBoolean;
        //String yearBoolean;
        String spliterBoolean;

        String day;
        String month;
        String year;
        String spliter;

        int indexSpliter = 0;


        List<String> dateParser = new ArrayList<>();

        if (dateCheck.contains(".")) {
            dateParser = List.of(dateCheck.split("\\."));
            indexSpliter = dateCheck.indexOf(".");
        }
        if (dateCheck.contains(",")) {
            dateParser = List.of(dateCheck.split(","));
            indexSpliter = dateCheck.indexOf(",");
        }
        if (dateCheck.contains("-")) {
            dateParser = List.of(dateCheck.split("-"));
            indexSpliter = dateCheck.indexOf("-");
        }


        dayCheck = dateParser.get(0).trim();
        monthCheck = dateParser.get(1).trim();
        yearCheck = dateParser.get(2).trim();
        spliterCheck = String.valueOf(dateCheck.charAt(indexSpliter)).trim();

        if (spliterCheck.equals(".") || spliterCheck.equals(",")) {
            spliter = ".";
            spliterBoolean = "true";
        } else if (spliterCheck.equals("-")) {
            spliter = ".";
            spliterBoolean = "true";
        } else {
            spliter = ".";
            spliterBoolean = "false";
        }


        if (Integer.parseInt(dayCheck) > 0 && Integer.parseInt(dayCheck) < 32) {
            day = dayCheck.length() == 1 ? "0" + dayCheck : dayCheck;
            dayBoolean = "true";

        } else {
            day = "00";
            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "You set False day " :
                    user.get().getLanguage().toLowerCase().equals("russian") ?"Вы задали не правильный день":"";
            buildMessage(chatId, answer);
            log.info(answer);
            dayBoolean = "false";
        }

        if (Integer.parseInt(monthCheck) > 0 && Integer.parseInt(monthCheck) < 13) {
            month = monthCheck.length() == 1 ? "0" + monthCheck : monthCheck;
            monthBoolean = "true";
        } else {
            month = "00";
            String answer = user.get().getLanguage().toLowerCase().equals("englisch") ? "You set False Month " :
                    user.get().getLanguage().toLowerCase().equals("russian") ? "Ввели не правильный месяц":"";
            buildMessage(chatId, answer);
            log.info("You set False Month ");
            monthBoolean = "false";
        }

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());


        String datapar = timeStamp.toString();
        String[] dateParseSplit = datapar.split(" ");
        String[] date = dateParseSplit[0].split("-");

        String yearEX = date[0];
        String yearDopp;

        String yEN = yearEX.substring(2, 4);
        int yENInt = Integer.parseInt(yEN);

        String yearEditAdd20 = yearEX.substring(0, 2);

        int checkYear19 = Integer.parseInt(yearEditAdd20) - 1;
        String yearEditAdd19 = Integer.toString(checkYear19);

        int yE = Integer.parseInt(yearCheck);

        if (yE > yENInt) {
            yearDopp = yearEditAdd19;
        } else {
            yearDopp = yearEditAdd20;
        }

        year = yearCheck.length() == 2 ? yearDopp + yearCheck :
                yearCheck.length() == 4 ? yearCheck : yearEX;


        return day + spliter + month + spliter + year + "&" + dayBoolean + "/" + monthBoolean + "/" + spliterBoolean;
    }

    private String timeCheckFnk(Long chatId, String timeCheck) {
        String minutesBoolean;
        String hourBoolean;
        String spliterBoolean;
        String minutesCheck;
        String hourCheck;
        String spliterCheck;
        String minutes;
        String hour;
        String spliter = null;
        String hourNull = null;
        String minutesNull = null;

        Optional<User> user = Optional.of(userRepository.findById(chatId).get());

        int indexSpliter = 0;
        List<String> timeParser = new ArrayList<>();


        if (timeCheck.contains(":")) {
            timeParser = List.of(timeCheck.split(":"));
            indexSpliter = timeCheck.indexOf(":");
        }
        if (timeCheck.contains(";")) {
            timeParser = List.of(timeCheck.split(";"));
            indexSpliter = timeCheck.indexOf(";");
        }

        hourCheck = timeParser.get(0).trim();
        minutesCheck = timeParser.get(1).trim();
        spliterCheck = String.valueOf(timeCheck.charAt(indexSpliter)).trim();


        spliter = switch (spliterCheck) {
            case ";", "\\.", ":" -> ":";
            default -> null;
        };

        if (Integer.parseInt(hourCheck) >= 0 && Integer.parseInt(hourCheck) < 24) {
            hourNull = hourCheck;
            hourBoolean = "true";

        } else if (Integer.parseInt(hourCheck) == 24) {
            hourNull = "0";
            hourBoolean = "true";

        } else {
            hour = "..";
            hourBoolean = "false";
            buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ?"hour is false": user.get().getLanguage().toLowerCase().equals("russian") ? "Вы ввели не правильный час":"");
        }
        if (Integer.parseInt(minutesCheck) >= 0 && Integer.parseInt(minutesCheck) < 60) {
            minutesNull = minutesCheck;
            minutesBoolean = "true";
        } else {
            minutes = "..";
            minutesBoolean = "false";
            buildMessage(chatId, user.get().getLanguage().toLowerCase().equals("englisch") ? "minutes is false" : user.get().getLanguage().toLowerCase().equals("russian") ?"Вы ввели не правильные минуты":"");
        }
        if (Integer.parseInt(minutesCheck) == 60) {
            minutesNull = "00";
            hourNull = String.valueOf((Integer.parseInt(hourCheck)) + 1);
            minutesBoolean = "true";
        }
        assert spliter != null;
        spliterBoolean = "true";

        assert hourNull != null;
        hour = checkNullFnk(hourNull);
        assert minutesNull != null;
        minutes = checkNullFnk(minutesNull);

        return hour + spliter + minutes + "&" + hourBoolean + "/" + minutesBoolean + "/" + spliterBoolean;
    }

    private String checkNullFnk(String numb) {
        String res = null;
        if (numb != null) {
            if (numb.length() <= 1) {
                res = "0" + numb;
            } else if (numb.length() == 2) {
                res = numb;
            } else {
                res = "..";
            }
        } else {
            res = "..";
        }
        return res;
    }
    //Date

    private LocalTime regionsTime(Long chatId, LocalTime time) {
        Map<String, Integer> regions = getRegionsMap();
        Optional<User> user = userRepository.findById(chatId);
        if (regions.containsKey(user.get().getRegion())) {
            return time.minusHours(regions.get(user.get().getRegion()));
        }
        return time.minusHours(regions.get("Germany"));

    }

    protected LocalTime localTimeNow(Long chatId) {
        Map<String, Integer> regions = getRegionsMap();
        Optional<User> user = userRepository.findById(chatId);
        if (regions.containsKey(user.get().getRegion())) {
            return LocalTime.now().plusHours(regions.get(user.get().getRegion()));
        }
        return LocalTime.now().plusHours(regions.get("Germany"));
    }

    private static Map<String, Integer> getRegionsMap() {
        int russianHours = getRussianHours();
        Map<String, Integer> regions = new HashMap<>();
        regions.put("Russia", +russianHours);
        regions.put("Germany", 0);
        return regions;
    }

    private static int getRussianHours() {
        LocalDate localDate = LocalDate.now();
        int letsSundayNov = getLetsSunday(localDate.getYear(), Calendar.NOVEMBER);
        int letsSundayMarch = getLetsSunday(localDate.getYear(), Calendar.MARCH);

        LocalDate november = LocalDate.of(localDate.getYear(), 11, letsSundayNov);
        LocalDate march = LocalDate.of(localDate.getYear(), 3, letsSundayMarch);

        int russianHours = 0;

        if (Period.between(localDate, november).getYears() <= 0
                && Period.between(localDate, november).getMonths() <= 0
                && Period.between(localDate, november).getDays() <= 0) {
            russianHours = 2;
        } else if (Period.between(localDate, march).getYears() <= 0
                && Period.between(localDate, march).getMonths() <= 0
                && Period.between(localDate, march).getDays() <= 0) {
            russianHours = 1;
        }

        return russianHours;
    }

    public static int getLetsSunday(int years, int month) {

        // Создание объекта Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, years);
        calendar.set(Calendar.MONTH, month);

        // Установка дня на последний день месяца
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Поиск последнего воскресенья
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        // Получение результатов
        return calendar.get(Calendar.DAY_OF_MONTH);

    }

    //SendMessage
    public void buildMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        sendMessage(sendMessage);
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private static void clearSelectedcommend() {
        selectedCommands.clear();
        entityId.clear();
    }

}


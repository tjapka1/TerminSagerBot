package com.tjapka.TerminSagerBot.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
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
public class Bot extends TelegramLongPollingBot {
    static final String HELP_TEXT_START = """
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
        if (messageTextCheck.contains("@TjapkaTerminsager88bot")) {
            int i = messageTextCheck.indexOf("@");
            messageText = messageTextCheck.substring(0, i);


        } else {
            messageText = messageTextCheck;
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
        List<String> fnkUserQ = List.of("/setregion", "/deleteuserdata", "/username", "/firstname", "/lastname", "/beditforeverydaystime", "/beditfornowdaytime", "/teditforeverydaystime");
        fnkUser.addAll(fnkUserQ);
        List<String> fnkTermin = List.of("/newtermin", "/showtermin", "/deletetermin", "/deletethistermin", "/teditname", "/teditdate", "/teditminusmin", "/tedittime", "/teditfordays");
        List<String> fnkBirthDay = List.of("/newbirthday", "/showbirthday", "/deletebirthday", "/deletethisbirthday", "/beditfirstname", "/beditlastname", "/beditdate", "/beditfordays");
        List<String> fnkReminder = List.of("/newreminder", "/showreminder", "/deletereminder", "/deletethisreminder", "/reditname", "/redittime", "/reditdays", "/reditminutes");
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
            log.info(userName + ": Type this message: " + messageText);

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
        switch (messageText) {
            case "/registrade":
                registerUser(message);
                break;
            case "/username":
                buildMessage(chatId, "Please enter your username.");
                break;
            case "/firstname":
                buildMessage(chatId, "Please enter your firstname.");
                break;
            case "/lastname":
                buildMessage(chatId, "Please enter your lastname.");
                break;
            case "/beditforeverydaystime":
                buildMessage(chatId, "Please enter time for birthday reminder ever Days.");
                break;
            case "/beditfornowdaytime":
                buildMessage(chatId, "Please enter time for birthday reminder now Days.");
                break;
            case "/teditforeverydaystime":
                buildMessage(chatId, "Please enter time for termin reminder ever Days.");
                break;

            case "/mydata":
                myDataUser(chatId);
                clearSelectedcommend();
                break;
            case "/setregion":
                buildMessage(chatId, """
                        please set your region\s
                        Russia
                        Germany"""
                );
                break;
            case "/deleteuserdata":
                Optional<User> user = Optional.of(userRepository.findById(chatId).get());
                Long userId = user.get().getId();
                String idLenght = userId.toString();
                String userIdEit = userId.toString().substring(idLenght.length() - 4, idLenght.length());
                long userIdCheck = Long.parseLong(userIdEit);
                buildMessage(chatId, "do you want to delete all your data? \nPlease enter this number\n" + userIdCheck);
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    private void terminFunk(String fnkBTN, long chatId, String userName) throws ParseException {
        switch (fnkBTN) {
            case "/newtermin":
                buildMessage(chatId, "Please enter your termin! \nExample:  \nTermin Name, Termin Date, Termin Time\nTermin Name, 00.00.0000, 00:00");
                break;
            case "/showtermin":
                showAllTermins(chatId);
                break;
            case "/teditname":
                buildMessage(chatId, "Please enter new Termin Name");
                break;
            case "/teditdate":
                buildMessage(chatId, "Please enter new date for Termin");
                break;
            case "/tedittime":
                buildMessage(chatId, "Please enter new time for Termin");
                break;
            case "/teditminusmin":
                buildMessage(chatId, "Please enter Minutes for Terminn");
                break;
            case "/teditfordays":
                buildMessage(chatId, "Please enter Days for Terminn");
                break;
            case "/deletethistermin":
                break;
            case "/deletetermin":
                if (!terminRepository.findByUserId(chatId).isEmpty()) {
                    showAllTermins(chatId);
                    buildMessage(chatId, "Please enter your terminID Of delete");
                } else {
                    buildMessage(chatId, "You have not Termins");
                    clearSelectedcommend();
                }
                break;
            default:
                defaultComand(chatId, userName, fnkBTN);
                break;
        }
    }

    private void birthDayFunk(String messageText, long chatId, String userName) {
        switch (messageText) {
            case "/newbirthday":
                buildMessage(chatId, "Please enter new Birthday! \nExample:  \nPerson Firstname, Person Lastname, Birthday Date\nExample Firstname, Example Lastname, 00.00.0000");
                break;
            case "/showbirthday":
                showAllBirthday(chatId);
                break;
            case "/beditfirstname":
                buildMessage(chatId, "Please enter Birthday new Firstname");
                break;
            case "/beditlastname":
                buildMessage(chatId, "Please enter Birthday new Lastname");
                break;
            case "/beditdate":
                buildMessage(chatId, "Please enter Birthday new Date");
                break;
            case "/beditfordays":
                buildMessage(chatId, "Please enter Birthday new days for Birthday");
                break;
            case "/deletebirthday":
                if (!birthdayRepository.findByUserId(chatId).isEmpty()) {
                    showAllBirthday(chatId);
                    buildMessage(chatId, "Please enter your BirthDayID Of delete");
                } else {
                    buildMessage(chatId, "You have not Birthdays");
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
        switch (messageText) {
            case "/newreminder":
                buildMessage(chatId, """
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

                        Reminder name, weekend, 00:00""");
                break;
            case "/showreminder":
                showAllReminder(chatId);
                break;
            case "/deletereminder":
                if (!reminderRepository.findByUserId(chatId).isEmpty()) {
                    showAllReminder(chatId);
                    buildMessage(chatId, "Please enter your ReminderID Of delete");
                } else {
                    buildMessage(chatId, "You have not Birthdays");
                    clearSelectedcommend();
                }
                break;
            case "/reditminutes":
                buildMessage(chatId, "Please enter your Reminder minutes");
                break;
            case "/reditname":
                buildMessage(chatId, "Please enter your Reminder new name");
                break;
            case "/redittime":
                buildMessage(chatId, "Please enter your Reminder new time");
                break;
            case "/deletethisreminder":
                break;
            case "/reditdays":
                buildMessage(chatId, """
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
                        weekend = for Weekend Days""");
                break;
            default:
                defaultComand(chatId, userName, messageText);
                break;

        }
    }

    private void buildNewTermin(Message message, long chatId) {

        String parseString = message.getText();

        List<String> parser = List.of(parseString.split(", "));

        String terminName = parser.get(0).trim();
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

        String firstName = parser.get(0).trim();
        String lastName = parser.get(1).trim();
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

        String reminderTittle = parser.get(0).trim();
        String reminderDaysCheck = parser.get(1).trim();
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

        String timeCheck = parser.get(2).trim();

        String checktimeAll = timeCheckFnk(chatId, timeCheck);
        List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
        List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
        String setTime = timeTrueFalse.get(0);

        if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {


            String answer = "Use" +
                    "r: " + message.getFrom().getUserName() + " " +
                    "created a new Reminder: " + reminderTittle + " " + reminderDays + " " + setTime;


            log.info(answer);
            addReminder(chatId, reminderTittle, reminderDays, setTime);


            if (trueFalseTime.get(0).equals("false") && trueFalseTime.get(1).equals("false")) {
                String answerLog = "User set false Time";
                log.info(answerLog);
            }
        }
        selectedCommands.clear();
    }

    private void addTermin(long chatId, String terminName, String terminDate, String terminTime) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;

            user = Optional.of(userRepository.findById(chatId).get());
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

            String answer = user.get().getUserName() + " Termin saved!!!\n" + answerTermin(save);
            String answerLog = user.get().getUserName() + " Termin saved: " + terminName;
            log.info(answerLog);
            buildMessage(chatId, answer);
        }
    }

    private void addBirthday(long chatId, String firstName, String lastName, String setDate) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            Birthday birthday = Birthday.builder()
                    .birthdayFirstName(firstName)
                    .birthdayLastName(lastName)
                    .birthdayDate(setDate)
                    .birthdayMinusDay("1 3 5")
                    .user(user.get())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            Birthday save = birthdayRepository.save(birthday);

            String answer = user.get().getUserName() + " | Birthday saved!!!\n" + answerBirthday(save);
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

    private void showAllTermins(long chatId) {
        List<Termin> userTermins = terminRepository.findByUserId(chatId);
        String answer;
        if (!userTermins.isEmpty()) {
            for (Termin termin : userTermins) {
                answer = answerTermin(termin);
                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Termins: " + userTermins.size());
        } else {
            answer = "you have not Termins";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void showAllBirthday(long chatId) {
        List<Birthday> userBirthdays = birthdayRepository.findByUserId(chatId);
        String answer;
        if (!userBirthdays.isEmpty()) {
            for (Birthday birthday : userBirthdays) {
                answer = answerBirthday(birthday);
                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Birthdays: " + userBirthdays.size());
        } else {
            answer = "you have not Birthdays";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void showAllReminder(long chatId) {
        List<Reminder> userReminder = reminderRepository.findByUserId(chatId);
        String answer;
        if (!userReminder.isEmpty()) {
            for (Reminder reminder : userReminder) {
                answer = answerReminder(reminder);

                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Reminders: " + userReminder.size());
        } else {
            answer = "you have not Reminders";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void editRemindermindMinuts(Long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()) {
            reminder.get().setReminderMinusMin(messageText);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit Minutes: " + reminder.get().getReminderMinusMin() + "\n" +
                    answerReminder(reminder.get());

            String answerLog = "reminder edit Minutes: " + reminder.get().getReminderMinusMin() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();

    }

    private void editRemindermindName(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()) {
            reminder.get().setReminderTittle(messageText);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit name: " + reminder.get().getReminderTittle() + "\n" +
                    answerReminder(reminder.get());

            String answerLog = "reminder edit Name: " + reminder.get().getReminderTittle() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }

    private void editRemindermindTime(long chatId, String editremindermind, String timeReminder) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
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

                answer = "Reminder edit Reminder time: " + reminder.get().getReminderTime() + "\n" +
                        answerReminder(reminder.get());

                answerLog = "Reminder edit Reminder time: " + reminder.get().getReminderTime() + " ID: " + reminder.get().getId();
            }

            if (trueFalseTime.get(0).equals("false") || trueFalseTime.get(1).equals("false")) {

                answer = "Reminder time: " + reminder.get().getReminderTime() + " | /redittime_" + reminder.get().getId();
                answerLog = "User set false Time";

            }


        }
        buildMessage(chatId, answer);
        log.info(answerLog);
        clearSelectedcommend();
    }

    private void editRemindermindDays(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
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

            reminder.get().setReminderDays(reminderDays);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit days: " + reminder.get().getReminderDays() + "\n" +
                    answerReminder(reminder.get());
            String answerLog = "reminder edit Days: " + reminder.get().getReminderDays() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }

    private void editBirthdayFirstname(long chatId, String birthdayId, String firstName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        if (!firstName.isEmpty()) {
            birthday.get().setBirthdayFirstName(firstName);
            birthdayRepository.save(birthday.get());

            String answer = "birthday edit Firstname: " + birthday.get().getBirthdayFirstName() + "\n" +
                    answerBirthday(birthday.get());

            String answerLog = "birthday edit Firstname: " + birthday.get().getBirthdayFirstName() + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private void editBirthdayLastname(long chatId, String birthdayId, String lastName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        if (!lastName.isEmpty()) {
            birthday.get().setBirthdayLastName(lastName);
            birthdayRepository.save(birthday.get());

            String answer = "birthday edit Lastname: " + birthday.get().getBirthdayLastName() + "\n" +
                    answerBirthday(birthday.get());

            String answerLog = "birthday edit Lastname: " + birthday.get().getBirthdayLastName() + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editBirthdayDate(long chatId, String birthdayId, String birthdayDateCheck) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
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

                answer = "Birthday edit Birthday date: " + birthday.get().getBirthdayDate() + "\n" +
                        answerBirthday(birthday.get());

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
        if (!birthdayMinusDays.isEmpty()) {
            birthday.get().setBirthdayMinusDay(birthdayMinusDays);
            birthdayRepository.save(birthday.get());

            String answerStandart = "birthday edit for birthday days: " + birthday.get().getBirthdayMinusDay();

            String answer = answerStandart + "\n" +
                    answerBirthday(birthday.get());

            String answerLog = answerStandart + " ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();


    }

    private void editTerminName(long chatId, String terminId, String terminName) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminName.isEmpty()) {
            termin.get().setTerminName(terminName);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin name: " + termin.get().getTerminName() + "\n" +
                    answerTermin(termin.get());

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
        if (!terminDateCheck.isEmpty()) {

            String checkDataAll = dateCheckFnk(chatId, terminDateCheck);

            List<String> dataTrueFalse = List.of(checkDataAll.split("&"));


            List<String> trueFalse = List.of(dataTrueFalse.get(1).split("/"));

            String setDate = dataTrueFalse.get(0);

            termin.get().setTerminDate(setDate);
            terminRepository.save(termin.get());

            if (trueFalse.get(0).equals("true") && trueFalse.get(1).equals("true")) {

                answer = "Termin edit Termin date: " + termin.get().getTerminDate() + "\n" +
                        answerTermin(termin.get());

                answerLog = "Termin edit Termin date: " + termin.get().getTerminDate() + " ID: " + termin.get().getId();

            } else {
                answer = "Termin date: " + termin.get().getTerminDate() + " | /teditdate_" + termin.get().getId();
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
        if (!terminTime.isEmpty()) {

            String checktimeAll = timeCheckFnk(chatId, terminTime);
            List<String> timeTrueFalse = List.of(checktimeAll.split("&"));
            List<String> trueFalseTime = List.of(timeTrueFalse.get(1).split("/"));
            String setTime = timeTrueFalse.get(0);

            termin.get().setTerminTime(setTime);
            terminRepository.save(termin.get());

            if (trueFalseTime.get(0).equals("true") && trueFalseTime.get(1).equals("true")) {


                answer = "Termin edit Termin time: " + termin.get().getTerminTime() + "\n" +
                        answerTermin(termin.get());

                answerLog = "Termin edit Termin time: " + termin.get().getTerminTime() + " ID: " + termin.get().getId();

            }

            if (trueFalseTime.get(0).equals("false") || trueFalseTime.get(1).equals("false")) {
                answer = "Termin time: " + termin.get().getTerminTime() + " | /tedittime_" + termin.get().getId();
                answerLog = "User set false time";

            }
        }
        buildMessage(chatId, answer);
        log.info(answerLog);
        clearSelectedcommend();
    }

    private void editTerminMinusMin(long chatId, String terminId, String terminMinusMin) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminMinusMin.isEmpty()) {
            termin.get().setTerminMinusMin(terminMinusMin);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin Minutes: " + termin.get().getTerminTime() + "\n" +
                    answerTermin(termin.get());

            String answerLog = "Termin edit Termin Minutes: " + termin.get().getTerminTime() + " ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editTerminForDays(long chatId, String terminId, String terminMinusdays) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminMinusdays.isEmpty()) {
            termin.get().setTerminMinusDay(terminMinusdays);
            terminRepository.save(termin.get());

            String answerStandard = "Termin edit Termin Minutes: " + termin.get().getTerminMinusDay();
            String answer = answerStandard + "\n" +
                    answerTermin(termin.get());

            String answerLog = answerStandard + " ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }

    private static String answerTermin(Termin termin) {
        String answer;
        String createdDate = termin.getCreatedAt().toString();
        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));

        String time = parser.get(1);
        List<String> timeList = List.of(time.split(":"));

        answer =
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
                        "Delete this Termin /deletethistermin_" + termin.getId();
        return answer;
    }

    private static String answerBirthday(Birthday birthday) {
        String answer;
        String createdDate = birthday.getCreatedAt().toString();

        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));


        answer =
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
                        "Delete this Birthday /deletethisbirthday_" + birthday.getId();
        return answer;
    }

    private static String answerReminder(Reminder reminder) {

        String createdDate = reminder.getCreatedAt().toString();

        List<String> parser = List.of(createdDate.split(" "));

        String date = parser.get(0);
        List<String> dateList = List.of(date.split("-"));

        String answer;
        answer =
                "----------------------------" + " \n" +
                        "ID : " + reminder.getId() + "\n" +
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
        String answer;
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            List<Termin> termins = terminRepository.findByUserId(chatId);
            List<Birthday> birthdays = birthdayRepository.findByUserId(chatId);
            List<Reminder> reminders = reminderRepository.findByUserId(chatId);


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

            answer = "this is your saved data\n" +
                    "----------------------------" + " \n" +
                    id + " \n" +
                    userNameInfo + " \n" +
                    firstNameInfo + " \n" +
                    lastNameInfo + " \n" +

                    "----------------------------" + " \n" +

                    "Region: " + user.get().getRegion() + "\n set you /setregion for your correctly Time" + "\n" +
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
        } else {
            answer = "you are not in our database yet,\n" +
                    "if you want to register, then click /registrade ";
        }
        buildMessage(chatId, answer);
    }

    private void deleteUserData(Long chatId, String messageText) {

        if (containsDigitsRegex(messageText)) {
            long userEnterId = Long.parseLong(messageText);

            Optional<User> user = Optional.of(userRepository.findById(chatId).get());

            Long userId = user.get().getId();

            String idLenght = userId.toString();


            String userIdEit = userId.toString().substring(idLenght.length() - 4, idLenght.length());

            long userIdCheck = Long.parseLong(userIdEit);

            if (userIdCheck == userEnterId) {
                deleteUser(chatId);
            } else {
                buildMessage(chatId, "you entered the wrong numbers");
            }
        } else if (!containsDigitsRegex(messageText)) {
            buildMessage(chatId, "\"you entered the wrong numbers\"");
        } else {
            buildMessage(chatId, "\"you entered the wrong numbers\"");
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
            String answer = "your data has been deleted from the database " + user.get().getUserName();
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
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .region("null")
                    .terminFordaysTime("18:00")
                    .birthdayFordaysTime("18:00")
                    .birthdayNowDayTime("18:00")
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("User save: " + user);
            buildMessage(chatId, "User save: " + user.getUserName());
        }
    }

    private void setUsername(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String userName = message.getText();
        if (!userName.isEmpty()) {
            user.get().setUserName(userName);
            userRepository.save(user.get());

            String answer = "User: " + user.get().getUserName() + " set Username: " + userName;
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
            user.get().setFirstName(firstName);
            userRepository.save(user.get());

            String answer = "User: " + user.get().getUserName() + " set Firstname: " + firstName;
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
            user.get().setLastName(lastName);
            userRepository.save(user.get());

            String answer = "User: " + user.get().getUserName() + " set Lastname: " + lastName;
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
            answer = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;
            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setBirthdayFordaysTime(saveTime);
            userRepository.save(user.get());

            answer = "User set false time | Edit /beditforeverydaystime ";
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
            answer = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;
            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setBirthdayNowDayTime(saveTime);
            userRepository.save(user.get());

            answer = "User set false time | Edit /beditforeverydaystime ";
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
            answer = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;
            answerLog = "User: " + user.get().getUserName() + " set time for every day reminder: " + saveTime;

            buildMessage(chatId, answer);
            log.info(answerLog);
            myDataUser(chatId);
        } else {
            saveTime = "18:00";
            user.get().setTerminFordaysTime(saveTime);
            userRepository.save(user.get());

            answer = "User set false time | Edit /teditforeverydaystime";
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
        if (timesList.size() == trueCount) {
            res = true;
        } else {
            res = false;
        }

        return res;
    }


    private void setUserRegion(Message message, long chatId) {
        Optional<User> user = userRepository.findById(chatId);
        String region = message.getText();
        if (!region.isEmpty()) {
            user.get().setRegion(region);
            userRepository.save(user.get());

            String answer = "User: " + user.get().getUserName() + " set region: " + region;
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();

    }

    //StartFunk
    private void help(long chatId) {
        log.info("Replied to user: ");
        buildMessage(chatId, HELP_TEXT_START);
    }

    private void start(long chatId) {

        Optional<User> user = userRepository.findById(chatId);

        String userName = user.get().getUserName();
        String setRegion = user.get().getRegion().equalsIgnoreCase("null") ?
                "Please set you Region for correctly time! /setregion" : "";

        String answer = user.get().getUserName().equalsIgnoreCase("null") ? "HI, your username is null, please set you /username" + setRegion :
                "Hi. " + userName + " nice to meet you.\n" + setRegion;

        log.info("Replied to user: " + userName);
        buildMessage(chatId, answer);
    }

    private void defaultComand(long chatId, String userName, String messageText) {
        String answer = "Sorry, command was not recognized";
        log.info("Replied to user: " + userName + " user falls command " + messageText);
        buildMessage(chatId, answer);
    }


    @Scheduled(cron = ("0 */1 * * * *"))
    private void reminderSender() {
        List<Reminder> allReminders = reminderRepository.findAll();
        List<Integer> reminderDays = new ArrayList<>();

        if (!allReminders.isEmpty()) {
            for (Reminder reminder : allReminders) {

                LocalDate localDate = LocalDate.now();
                LocalTime localTime = LocalTimeNow(reminder.getUser().getId());

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

                sendReminder(reminder, minusMinSort, reminderTime, reminderDays, weekDayNow, localTime);

                reminderDays.clear();

            }
        }
    }

    @Scheduled(cron = ("0 */1 * * * *"))
    private void terminSender() {

        List<Termin> allTermins = terminRepository.findAll();
        if (!allTermins.isEmpty()) {
            for (Termin termin : allTermins) {

                LocalDate localDate = LocalDate.now();
                LocalTime localTime = LocalTimeNow(termin.getUser().getId());

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
                sendTermin(termin, minusMinSort, terminTime, localDate, terminDate, localTime);
            }
        }
    }

    @Scheduled(cron = ("0 */1 * * * *"))
    private void terminSenderDays() {
        List<Termin> allTermins = terminRepository.findAll();
        LocalDate expectedDay;
        LocalDate localDate = LocalDate.now();
        String terminForDaysTime;
        List<String> parseTime;
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
                                    .time(forDaysTime)
                                    .build());



                            }
                        }
                    }
                }
            }
        }
    }



    private void sendReminder(Reminder reminder, List<String> minusMinSort, LocalTime reminderTime, List<Integer> reminderDays, int weekDayNow, LocalTime localTime) {
        int count = 0;
        for (String s : minusMinSort) {
            LocalTime reminderTimeMin;
            reminderTimeMin = s.equals("0") ? reminderTime : reminderTime.minusMinutes(Integer.parseInt(
                    s));
            if (
                    reminderDays.contains(weekDayNow)

            ) {
                Long chatId = reminder.getUser().getId();

                String answer = "In " + s + " minutes it will be | " + reminder.getReminderTittle() + " | at " + reminder.getReminderTime() + " o'clock.";
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Reminder")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(reminderTimeMin)
                        .build());
            }
            count++;
        }
        if (minusMinSort.isEmpty() || minusMinSort.get(0).equals("0") || minusMinSort.size() == count) {
            if (
                    reminderDays.contains(weekDayNow)

            ) {
                Long chatId = reminder.getUser().getId();
                String answer = "Now " + reminder.getReminderTittle() + " " + reminder.getReminderTime();

                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Reminder")
                        .messageToSend(answer)
                        .messageToLog(answer)
                        .time(reminderTime)
                        .build());

            }
        }

    }

    private void sendTermin(Termin termin, List<String> minusMinSort, LocalTime terminTime, LocalDate localDate, LocalDate terminDate, LocalTime localTime) {
        int count = 0;

        for (String s : minusMinSort) {

            LocalTime terminTimeMin = s.equals("0") ? terminTime : terminTime.minusMinutes(Integer.parseInt(
                    s));

            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0
            ) {
                Long chatId = termin.getUser().getId();
                String answer = termin.getTerminName() + "\nIn " + s + " minutes it will be" + "\nat " + termin.getTerminTime() + " o'clock\nDate: " + termin.getTerminDate();
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Termin")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(terminTimeMin)
                        .build());
            }
            count++;
        }

        if (minusMinSort.isEmpty() || minusMinSort.get(0).equals("0") || minusMinSort.size() == count) {
            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0
            ) {
                Long chatId = termin.getUser().getId();
                String answer = termin.getTerminName() + "\nNow \n" + "at " + termin.getTerminTime() + " o'clock\nDate: " + termin.getTerminDate();
                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Termin")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(terminTime)
                        .build());
            }
        }
    }


    @Scheduled(cron = ("0 */1 * * * *"))
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
                                            .time(forDaysTime)
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
                String answer = "today " + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLastName() + " birthday |  " + birthday.getBirthdayDate();

                messageStack.add(MessageToSend.builder()
                        .chatId(chatId)
                        .messageTyp("Birthday")
                        .messageToSend(answer)
                        .messageToLog("")
                        .time(nowDayTime)
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


    // TODO: 09.01.2024 sort
    @Scheduled(cron = ("0 */1 * * * *"))
    @Async
    protected void messageStackSort() {
        if (!messageStack.isEmpty()) {

            List<MessageToSend> sortedTime = messageStack.stream()
                    .sorted(Comparator.comparing(MessageToSend::getTime))
                    .toList();

            for (MessageToSend message : sortedTime) {

                System.out.println("ID: " + message.getChatId()/*+" MSG "+message.getMessageToSend()*/ + " Typ " + message.getMessageTyp() + " time " + message.getTime());

                if (
                        ChronoUnit.HOURS.between(LocalTime.now(), message.getTime()) == 0
                     && ChronoUnit.MINUTES.between(LocalTime.now(), message.getTime()) == 0
                     && ChronoUnit.SECONDS.between(LocalTime.now(), message.getTime()) == 0
                ) {
                    buildMessage(message.getChatId(), message.getMessageToSend());
                    log.info("Message send: " + message.getMessageToLog());
                }

            }
        }
        messageStack.clear();
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
        String dayCheck;
        String monthCheck;
        String yearCheck;
        String spliterCheck;

        String dayBoolean;
        String monthBoolean;
        String yearBoolean;
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
            String answer = "You set False day ";
            buildMessage(chatId, answer);
            log.info(answer);
            dayBoolean = "false";
        }

        if (Integer.parseInt(monthCheck) > 0 && Integer.parseInt(monthCheck) < 13) {
            month = monthCheck.length() == 1 ? "0" + monthCheck : monthCheck;
            monthBoolean = "true";
        } else {
            month = "00";
            String answer = "You set False Month ";
            buildMessage(chatId, answer);
            log.info(answer);
            monthBoolean = "false";
        }

        System.out.println(messageStack);


        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());


        String datapar = timeStamp.toString();
        System.out.println(datapar);
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
            case ";" -> ":";
            case ":" -> ":";
            case "\\." -> ":";
            default -> spliter;
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
            buildMessage(chatId, "hour is false");
        }
        if (Integer.parseInt(minutesCheck) >= 0 && Integer.parseInt(minutesCheck) < 60) {
            minutesNull = minutesCheck;
            minutesBoolean = "true";
        } else {
            minutes = "..";
            minutesBoolean = "false";
            buildMessage(chatId, "minutes is false");
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
    protected LocalTime LocalTimeNow(Long chatId) {
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

        int russianHours = 2;

        if (Period.between(localDate, november).getYears() == 0
                && Period.between(localDate, november).getMonths() == 0
                && Period.between(localDate, november).getDays() == 0) {
            russianHours = 2;
        } else if (Period.between(localDate, march).getYears() == 0
                && Period.between(localDate, march).getMonths() == 0
                && Period.between(localDate, march).getDays() == 0) {
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


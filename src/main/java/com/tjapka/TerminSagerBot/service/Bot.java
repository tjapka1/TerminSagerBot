package com.tjapka.TerminSagerBot.service;

import com.tjapka.TerminSagerBot.Entity.Birthday;
import com.tjapka.TerminSagerBot.Entity.Reminder;
import com.tjapka.TerminSagerBot.Entity.Termin;
import com.tjapka.TerminSagerBot.Entity.User;
import com.tjapka.TerminSagerBot.Repository.BirthdayRepository;
import com.tjapka.TerminSagerBot.Repository.ReminderRepository;
import com.tjapka.TerminSagerBot.Repository.TerminRepository;
import com.tjapka.TerminSagerBot.Repository.UserRepository;
import com.tjapka.TerminSagerBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EnableScheduling
@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
    static final String HELP_TEXT_START = "This bot is created to demonstrate Spring capabilities. \n\n" +
            "You can execute commands from the main menu on the left or by typing a command: \n\n" +
            "This is Start \n\n" +
            "Type /start to see a welcome message\n\n"+
            "Type /help to see this message again"
            ;

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
    static List<String>selectedCommands = new ArrayList<>(1);
    static List <String> entityId = new ArrayList<>(1);

    public Bot(BotConfig config) {

        this.config = config;

        //Меню Комманд

            List<BotCommand> listOfCommands = new ArrayList<>();
            listOfCommands.add(new BotCommand("/start", "get a welcome message"));
            listOfCommands.add(new BotCommand("/mydata", "get my Data storage"));
            listOfCommands.add(new BotCommand("/setregion", "set your region"));
            listOfCommands.add(new BotCommand("/setusername", "set your username"));
            listOfCommands.add(new BotCommand("/deleteuserdata", "delete my Data"));
            listOfCommands.add(new BotCommand("/newtermin", "set new Termin"));
            listOfCommands.add(new BotCommand("/showtermin", "View alls Termins"));
            listOfCommands.add(new BotCommand("/deletetermin", "delete one Termin"));
            listOfCommands.add(new BotCommand("/newbirthday", "set new Birthday"));
            listOfCommands.add(new BotCommand("/showbirthday", "View alls Birthday"));
            listOfCommands.add(new BotCommand("/deletebirthday", "delete one birthday"));
            listOfCommands.add(new BotCommand("/newreminder", "set new reminder"));
            listOfCommands.add(new BotCommand("/showreminder", "View alls reminder"));
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
           if (update.hasMessage() && update.getMessage().hasText()){
            handleMessage(update.getMessage());
        }

    }
    private void handleMessage(Message message) {
        long chatId = message.getChatId();
        String userName = message.getFrom().getUserName();
        String fnkBTN="";


        String messageTextCheck = message.getText();
        String messageText = "";
        if (messageTextCheck.contains("@TjapkaTerminsager88bot")){
            int i = messageTextCheck.indexOf("@");
            messageText = messageTextCheck.substring(0, i);


        } else {messageText = messageTextCheck;}

        if (messageText.contains("/reditminutes")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/reditminutes";
            }
        if (messageText.contains("/reditname")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/reditname";
            }
        if (messageText.contains("/redittime")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/redittime";
            }
        if (messageText.contains("/reditdays")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/reditdays";
            }
        if (messageText.contains("/beditfirstname")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/beditfirstname";

        }
        if (messageText.contains("/beditlastname")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/beditlastname";

        }
        if (messageText.contains("/beditdate")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/beditdate";

        }
        if (messageText.contains("/teditname")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/teditname";

        }
        if (messageText.contains("/teditdate")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/teditdate";
        }
        if (messageText.contains("/teditminusmin")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/teditminusmin";
        }
        if (messageText.contains("/tedittime")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/tedittime";

        }
        if (messageText.contains("/deletethisreminder")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/deletethisreminder";
            deleteThisReminder(chatId, entityId.get(0));
            clearSelectedcommend();
        }

        if (messageText.contains("/deletethisbirthday")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/deletethisbirthday";
            deleteThisBirthday(chatId, entityId.get(0));
            clearSelectedcommend();
        }
        if (messageText.contains("/deletethistermin")){
            int s = messageText.indexOf("_");
            entityId.add(messageText.substring(s+1));
            messageText="/deletethistermin";
            deleteThisTermin(chatId, entityId.get(0));
            clearSelectedcommend();
        }



        //------------------------------------------------
        List<String>fnkStart=List.of("/start", "/help");
        List<String>fnkUser= new ArrayList<>(List.of("/registrade", "/mydata"));
        List<String>fnkUserQ=List.of("/setregion", "/deleteuserdata", "/username", "/firstname", "/lastname");
        fnkUser.addAll(fnkUserQ);
        List<String>fnkTermin=List.of("/newtermin", "/showtermin", "/deletetermin", "/deletethistermin", "/teditname", "/teditdate", "/teditminusmin", "/tedittime");
        List<String>fnkBirthDay=List.of("/newbirthday", "/showbirthday", "/deletebirthday", "/deletethisbirthday", "/beditfirstname", "/beditlastname", "/beditdate");
        List<String>fnkReminder=List.of("/newreminder", "/showreminder", "/deletereminder", "/deletethisreminder", "/reditname", "/redittime", "/reditdays", "/reditminutes" );
        //----------------
        List<String>fnkAll= new ArrayList<>();
        fnkAll.addAll(fnkStart);
        fnkAll.addAll(fnkTermin);

        fnkAll.addAll(fnkUser);
        fnkAll.addAll(fnkBirthDay);
        fnkAll.addAll(fnkReminder);
        //------------------------------------------------

        List<String>parser = List.of(messageText.split(" "));
        String fnkCheck = parser.get(0);

        if (messageText.contains("/")){
            fnkBTN=messageText ;
        }

        if (parser.size()>2){
            fnkBTN=fnkCheck;
        }

        //Проверка на не существуюшейся функции
        if (messageText.contains("/") && !fnkAll.contains(fnkCheck)){
            fnkBTN="";
            defaultCo(chatId, userName, messageText);
        }
        if (messageText.equals("/newtermin")){
            selectedCommands.add("/newtermin");
        }
        if (messageText.equals("/deletetermin")){
            selectedCommands.add("/deletetermin");
        }
        if (messageText.equals("/newbirthday")){
            selectedCommands.add("/newbirthday");
        }
        if (messageText.equals("/deletebirthday")){
            selectedCommands.add("/deletebirthday");
        }
        if (messageText.equals("/newreminder")){
            selectedCommands.add("/newreminder");
        }
        if (messageText.equals("/deletereminder")){
            selectedCommands.add("/deletereminder");
        }
        if (messageText.equals("/setregion")){
            selectedCommands.add("/setregion");
        }
        if (messageText.equals("/deleteuserdata")){
            selectedCommands.add("/deleteuserdata");
        }
        if (messageText.equals("/username")){
            selectedCommands.add("/username");
        }
        if (messageText.equals("/firstname")){
            selectedCommands.add("/firstname");
        }
        if (messageText.equals("/lastname")){
            selectedCommands.add("/lastname");
        }
        if (messageText.equals("/reditminutes")){
            selectedCommands.add("/reditminutes");
        }
        if (messageText.equals("/reditname")){
            selectedCommands.add("/reditname");
        }
        if (messageText.equals("/redittime")){
            selectedCommands.add("/redittime");
                }
        if (messageText.equals("/reditdays")){
            selectedCommands.add("/reditdays");
                }
        if (messageText.equals("/beditfirstname")){
            selectedCommands.add("/beditfirstname");
                }
        if (messageText.equals("/beditlastname")){
            selectedCommands.add("/beditlastname");
                }
        if (messageText.equals("/beditdate")){
            selectedCommands.add("/beditdate");
                }
        if (messageText.equals("/teditname")){
            selectedCommands.add("/teditname");
                }
        if (messageText.equals("/teditdate")){
            selectedCommands.add("/teditdate");
                }
        if (messageText.equals("/tedittime")){
            selectedCommands.add("/tedittime");
                }
        if (messageText.equals("/teditminusmin")){
            selectedCommands.add("/teditminusmin");
                }


        //------------------------------------------------
        if (fnkStart.contains(fnkBTN)) {
            startFunk(message, fnkBTN, chatId, userName);
            selectedCommands.clear();

        }else if (fnkUser.contains(fnkBTN)){
            userFunk(message, fnkBTN, chatId, userName);


        }else if (fnkBirthDay.contains(fnkBTN)){
            birthDayFunk(fnkBTN, chatId, userName);

        }else if (fnkReminder.contains(fnkBTN)){
            reminderFunk(fnkBTN, chatId, userName);

        }else if(fnkTermin.contains(fnkBTN)){
            try {
                terminFunk(fnkBTN, chatId, userName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }else if(!messageText.contains("/") && !fnkAll.contains(fnkCheck) && selectedCommands.isEmpty()){
            //buildMessage(chatId, "Echo Answer: " + messageText );
            log.info(userName + ": Type this message: " + messageText);

        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newtermin")){
            buildNewTermin(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletetermin")){
            deleteTermin(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newbirthday")){
            buildNewBirthday(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletebirthday")){
            deleteBirthday(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newreminder")){
            buildNewReminder(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletereminder")){
            deleteOneReminder(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletethisreminder")){
            deleteOneReminder(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/setregion")){
            setUserRegion(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deleteuserdata")){
            deleteUserData(chatId, messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/username")){
            setUsername(chatId, message);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/firstname")){
            setfirstname(chatId, message);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/lastname")){
            setLastname(chatId, message);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditminutes")){
            editRemindermindMinuts(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditname")){
            editRemindermindName(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/redittime")){
            editRemindermindTime(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/reditdays")){
            editRemindermindDays(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditfirstname")){
            editBirthdayFirstname(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditlastname")){
            editBirthdayLastname(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/beditdate")){
            editBirthdayDate(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditname")){
           editTerminName(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditdate")){
            editTerminDate(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/tedittime")){
            editTerminTime(chatId, entityId.get(0), messageText);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/teditminusmin")){
            editTerminMinusMin(chatId, entityId.get(0), messageText);
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
                defaultCo(chatId, userName, messageText);
                break;

        }
    }
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
            case "/mydata":
                myDataUser(chatId);
                clearSelectedcommend();
                break;
            case "/setregion":
                buildMessage(chatId, "please set your region " +
                        "\nRussia" +
                        "\nGermany" 
                );
                break;
            case "/deleteuserdata":
                Optional<User> user = Optional.of(userRepository.findById(chatId).get());
                Long userId = user.get().getId();
                String idLenght = userId.toString();
                String userIdEit = userId.toString().substring(idLenght.length()-4, idLenght.length()) ;
                long userIdCheck = Long.parseLong(userIdEit);
                buildMessage(chatId, "do you want to delete all your data? then dial these numbers\n" + userIdCheck);
                break;
            default:
                defaultCo(chatId, userName, messageText);
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
            case "/deletethistermin":
                break;
            case "/deletetermin":
                if (!terminRepository.findByUserId(chatId).isEmpty()){
                    showAllTermins(chatId);
                    buildMessage(chatId, "Please enter your terminID Of delete");
                }else {
                    buildMessage(chatId, "You have not Termins" );
                    clearSelectedcommend();
                }
                break;
            default:
                defaultCo(chatId, userName, fnkBTN);
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
            case "/deletebirthday":
                if (!birthdayRepository.findByUserId(chatId).isEmpty()) {
                    showAllBirthday(chatId);
                    buildMessage(chatId, "Please enter your BirthDayID Of delete");
                }else {
                    buildMessage(chatId, "You have not Birthdays" );
                    clearSelectedcommend();
                }
                break;
            case "/deletethisbirthday":
                break;
            default:
                defaultCo(chatId, userName, messageText);
                break;

        }
    }
    private void reminderFunk(String messageText, long chatId, String userName) {
        switch (messageText) {
            case "/newreminder":
                buildMessage(chatId, "Please enter new Reminder!" +
                        "\nWeekDays" +
                        "\n1 = Monday" +
                        "\n2 = Tuesday" +
                        "\n3 = Wednesday" +
                        "\n4 = Thursday" +
                        "\n5 = Friday" +
                        "\n6 = Saturday" +
                        "\n7 = Sunday" +
                        "\nall = for all WeekDays" +
                        "\nworkdays = for Work Days" +
                        "\nweekend = for Weekend Days" +
                        " \nExample:  \nReminder name, Reminder days, Reminder time\nReminder name, 1 3 5 6 , 00:00" +
                        "\n\nReminder name, all, 00:00" +
                        "\n\nReminder name, workdays, 00:00"+
                        "\n\nReminder name, weekend, 00:00");
                break;
            case "/showreminder":
                showAllReminder(chatId);
                break;
            case "/deletereminder":
                if (!reminderRepository.findByUserId(chatId).isEmpty()) {
                    showAllReminder(chatId);
                    buildMessage(chatId, "Please enter your ReminderID Of delete");
                }else {
                    buildMessage(chatId, "You have not Birthdays" );
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
                buildMessage(chatId, "Please enter your Reminder new days\n" +
                        "\nWeekDays" +
                        "\n1 = Monday" +
                        "\n2 = Tuesday" +
                        "\n3 = Wednesday" +
                        "\n4 = Thursday" +
                        "\n5 = Friday" +
                        "\n6 = Saturday" +
                        "\n7 = Sunday" +
                        "\nall = for all WeekDays" +
                        "\nworkdays = for Work Days" +
                        "\nweekend = for Weekend Days");
                break;
            default:
                defaultCo(chatId, userName, messageText);
                break;

        }
    }

    private void buildNewTermin(Message message, long chatId){

        String parseString = message.getText();

        List<String>parser = List.of(parseString.split(", "));

        String terminName = parser.get(0).trim();
        String terminDate = parser.get(1).trim();
        String terminTime = parser.get(2).trim();

        String [] terminDateparser = terminDate.split("\\.");
        //dateForm

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        String datapar = timeStamp.toString();

        String setDate = dateParser(datapar, terminDateparser);

        String answer = "User: " + message.getFrom().getUserName() + " " +
                "created a new event: " + terminName + " " + setDate + " " + terminTime;

        log.info(answer);
        //buildMessage(chatId, answer);
        addTermin(chatId, terminName, setDate, terminTime);
        clearSelectedcommend();
    }
    private void buildNewBirthday(Message message, long chatId) {
        String parseString = message.getText();

        List<String>parser = List.of(parseString.split(", "));

        String firstName = parser.get(0).trim();
        String lastName = parser.get(1).trim();
        String birthdayDate = parser.get(2).trim();

        String [] terminDateparser = birthdayDate.split("\\.");
        //dateForm

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        String datapar = timeStamp.toString();

        String setDate = dateParser(datapar, terminDateparser);

        String answer = "User: " + message.getFrom().getUserName() + " " +
                "created a new Birthday: " + firstName + " " + lastName + " " + setDate;

        log.info(answer);
        //buildMessage(chatId, answer);
        addBirthday(chatId, firstName, lastName, setDate);
        clearSelectedcommend();
    }
    private void buildNewReminder(Message message, long chatId) {
        String parseString = message.getText();

        List<String>parser = List.of(parseString.split(", "));

        String reminderTittle = parser.get(0).trim();
        String reminderDaysCheck = parser.get(1).trim();
        String reminderDays = "";

        if (reminderDaysCheck.equalsIgnoreCase("all")){
            reminderDays = "1 2 3 4 5 6 7";
        }else if (reminderDaysCheck.equalsIgnoreCase("workdays")){
            reminderDays = "1 2 3 4 5";
        }else if (reminderDaysCheck.equalsIgnoreCase("weekend")){
            reminderDays = "6 7";
        }else {
            reminderDays = reminderDaysCheck;
        }



        String reminderTime = parser.get(2).trim();


        String answer = "User: " + message.getFrom().getUserName() + " " +
                "created a new Reminder: " + reminderTittle + " " + reminderDays + " " + reminderTime;

        log.info(answer);
        //buildMessage(chatId, answer);
        addReminder(chatId, reminderTittle, reminderDays, reminderTime);
        selectedCommands.clear();
    }

    private void addTermin(long chatId, String terminName, String terminDate, String terminTime ) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            Termin termin = Termin.builder()
                    .user(user.get())
                    .terminName(terminName)
                    .terminDate(terminDate)
                    .terminTime(terminTime)
                    .terminMinusMin("5 10 15")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            Termin save = terminRepository.save(termin);

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
        String answer="";
        if (!userTermins.isEmpty()) {
            for (Termin termin : userTermins) {
                answer = answerTermin(termin);
                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Termins: "+userTermins.size());
        }else {
            answer ="you have not Termins";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void showAllBirthday(long chatId) {
        List<Birthday> userBirthdays = birthdayRepository.findByUserId(chatId);
        String answer="";
        if (!userBirthdays.isEmpty()) {
            for (Birthday birthday : userBirthdays) {
                answer = answerBirthday(birthday);
                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Birthdays: "+userBirthdays.size());
        }else {
            answer ="you have not Birthdays";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void showAllReminder(long chatId) {
        List<Reminder> userReminder = reminderRepository.findByUserId(chatId);
        String answer="";
        if (!userReminder.isEmpty()) {
            for (Reminder reminder : userReminder) {
                answer = answerReminder(reminder);

                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Reminders: "+userReminder.size());
        }else {
            answer ="you have not Reminders";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }

    private void editRemindermindMinuts(Long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()){
            reminder.get().setReminderMinusMin(messageText);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit Minutes: " + reminder.get().getReminderMinusMin() +"\n" +
                    answerReminder(reminder.get());

            String answerLog = "reminder edit Minutes: " + reminder.get().getReminderMinusMin() +  " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();

    }
    private void editRemindermindName(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()){
            reminder.get().setReminderTittle(messageText);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit name: " + reminder.get().getReminderTittle()+"\n"+
                    answerReminder(reminder.get());

            String answerLog = "reminder edit Name: " + reminder.get().getReminderTittle() +  " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }
    private void editRemindermindTime(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()){
            reminder.get().setReminderTime(messageText);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit Time: " + reminder.get().getReminderTime()+"\n"+
                    answerReminder(reminder.get());
            String answerLog = "reminder edit Time: " + reminder.get().getReminderTime() + " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }
    private void editRemindermindDays(long chatId, String editremindermind, String messageText) {
        Optional<Reminder> reminder = reminderRepository.findById(Long.parseLong(editremindermind));
        if (!messageText.isEmpty()){
            String reminderDaysCheck = messageText;
            String reminderDays;
            if (reminderDaysCheck.equalsIgnoreCase("all")){
                reminderDays = "1 2 3 4 5 6 7";
            }else if (reminderDaysCheck.equalsIgnoreCase("workdays")){
                reminderDays = "1 2 3 4 5";
            }else if (reminderDaysCheck.equalsIgnoreCase("weekend")){
                reminderDays = "6 7";
            }else {
                reminderDays = reminderDaysCheck;
            }

            reminder.get().setReminderDays(reminderDays);
            reminderRepository.save(reminder.get());

            String answer = "reminder edit days: " + reminder.get().getReminderDays()+"\n"+
                    answerReminder(reminder.get());
            String answerLog = "reminder edit Days: "+ reminder.get().getReminderDays() +  " ID : " + reminder.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);

        }
        clearSelectedcommend();
    }

    private void editBirthdayFirstname(long chatId, String birthdayId, String firstName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        if (!firstName.isEmpty()){
            birthday.get().setBirthdayFirstName(firstName);
            birthdayRepository.save(birthday.get());

            String answer = "birthday edit Firstname: " + birthday.get().getBirthdayFirstName()+"\n"+
                    answerBirthday(birthday.get());

            String answerLog = "birthday edit Firstname: " + birthday.get().getBirthdayFirstName()+" ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
            }
        clearSelectedcommend();
    }
    private void editBirthdayLastname(long chatId, String birthdayId, String lastName) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        if (!lastName.isEmpty()){
            birthday.get().setBirthdayLastName(lastName);
            birthdayRepository.save(birthday.get());

            String answer = "birthday edit Lastname: " + birthday.get().getBirthdayLastName()+"\n"+
                    answerBirthday(birthday.get());

            String answerLog = "birthday edit Lastname: " + birthday.get().getBirthdayLastName()+" ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }
    private void editBirthdayDate(long chatId, String birthdayId, String birthdayDate) {
        Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));
        if (!birthdayDate.isEmpty()){
            String [] terminDateparser = birthdayDate.split("\\.");
            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
            String datapar = timeStamp.toString();
            String setDate = dateParser(datapar, terminDateparser);

            birthday.get().setBirthdayDate(setDate);

            birthdayRepository.save(birthday.get());

            String answer = "birthday edit Date: " + birthday.get().getBirthdayDate()+"\n"+
                    answerBirthday(birthday.get());

            String answerLog = "birthday edit Date: " + birthday.get().getBirthdayDate()+" ID: " + birthday.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private void editTerminName(long chatId, String terminId, String terminName) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminName.isEmpty()){
            termin.get().setTerminName(terminName);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin name: " + termin.get().getTerminName()+"\n"+
                    answerTermin(termin.get());

            String answerLog = "Termin edit Termin name: " + termin.get().getTerminName()+" ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }
    private void editTerminDate(long chatId, String terminId, String terminDate) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminDate.isEmpty()){
            String [] terminDateparser = terminDate.split("\\.");
            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
            String datapar = timeStamp.toString();
            String setDate = dateParser(datapar, terminDateparser);

            termin.get().setTerminDate(setDate);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin date: " + termin.get().getTerminDate()+"\n"+
                    answerTermin(termin.get());

            String answerLog = "Termin edit Termin date: " + termin.get().getTerminDate()+" ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }
    private void editTerminTime(long chatId, String terminId, String terminTime)     {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminTime.isEmpty()){
            termin.get().setTerminTime(terminTime);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin time: " + termin.get().getTerminTime()+"\n"+
                    answerTermin(termin.get());

            String answerLog = "Termin edit Termin time: " + termin.get().getTerminTime()+" ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();
    }
    private void editTerminMinusMin(long chatId, String terminId, String terminMinusMin) {
        Optional<Termin> termin = terminRepository.findById(Long.parseLong(terminId));
        if (!terminMinusMin.isEmpty()){
            termin.get().setTerminMinusMin(terminMinusMin);
            terminRepository.save(termin.get());

            String answer = "Termin edit Termin Minutes: " + termin.get().getTerminTime()+"\n"+
                    answerTermin(termin.get());

            String answerLog = "Termin edit Termin Minutes: " + termin.get().getTerminTime()+" ID: " + termin.get().getId();

            buildMessage(chatId, answer);
            log.info(answerLog);
        }
        clearSelectedcommend();

    }

    private static String answerTermin(Termin termin) {
        String answer;
        String createdDate = termin.getCreatedAt().toString();
        List<String>parser = List.of(createdDate.split(" "));

        String date =parser.get(0);
        List<String>dateList=List.of(date.split("-"));

        String time = parser.get(1);
        List<String>timeList=List.of(time.split(":"));

        answer =
                "----------------------------" +" \n"+
                "ID: " + termin.getId() + "\n" +
                "----------------------------" +" \n"+
                "Termin name: " + termin.getTerminName() + " | /teditname_"+termin.getId() + "\n" +
                "Termin date: " + termin.getTerminDate()  + " | /teditdate_"+termin.getId() + "\n" +
                "Termin time: " + termin.getTerminTime() + " | /tedittime_"+termin.getId() + "\n" +
                "Termin minuts: " + termin.getTerminMinusMin() + " | /teditminusmin_"+termin.getId() + "\n" +
                "----------------------------" +" \n"+
                "created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) + " " +
                timeList.get(0)+":"+timeList.get(1)+ "\n" +
                "----------------------------" +" \n"+
                "Delete this Termin /deletethistermin_"+termin.getId();;
        return answer;
    }
    private static String answerBirthday(Birthday birthday) {
        String answer;
        String createdDate = birthday.getCreatedAt().toString();

        List<String>parser = List.of(createdDate.split(" "));

        String date =parser.get(0);
        List<String>dateList=List.of(date.split("-"));


        answer =
                "----------------------------" +" \n"+
                "ID: " + birthday.getId() + "\n" +
                        "----------------------------" +" \n"+
                        "Firstname: " + birthday.getBirthdayFirstName() + " | /beditfirstname_"+birthday.getId() + "\n" +
                        "Lastname: " +birthday.getBirthdayLastName() + " | /beditlastname_"+birthday.getId() + "\n" +
                        "Birthday date: " + birthday.getBirthdayDate() + " | /beditdate_"+birthday.getId() +"\n"+
                        "----------------------------" +" \n"+
                        "created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) +"\n" +
                        "----------------------------" +" \n"+
                        "Delete this Birthday /deletethisbirthday_"+birthday.getId();
        return answer;
    }
    private static String answerReminder(Reminder reminder) {

        String createdDate = reminder.getCreatedAt().toString();

        List<String>parser = List.of(createdDate.split(" "));

        String date =parser.get(0);
        List<String>dateList=List.of(date.split("-"));

        String answer;
        answer =
                "----------------------------" +" \n"+
                "ID : " + reminder.getId() + "\n"+
                       "----------------------------" +" \n"+
                        "Tiitle: " + reminder.getReminderTittle() + " | /reditname_"+ reminder.getId() + "\n"+
                        "Days: " + reminder.getReminderDays() + " | /reditdays_"+ reminder.getId() + "\n"+
                        "Minuts: " + reminder.getReminderMinusMin()+ " | /reditminutes_"+ reminder.getId()+ "\n"+
                        "Time: " + reminder.getReminderTime() + " | /redittime_"+ reminder.getId() +"\n" +
                        "----------------------------" +" \n"+
                        "Created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) + "\n"+
                        "----------------------------" +" \n"+
                        "Delete this Reminder /deletethisreminder_"+reminder.getId();

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
        if (birthdayRepository.findById(Long.parseLong(birthdayId)).isPresent()){
            Optional<Birthday> birthday = birthdayRepository.findById(Long.parseLong(birthdayId));

            String answer = "Deleted Birthday: " + birthday.get().getId() + " " + birthday.get().getBirthdayFirstName()+" "+ birthday.get().getBirthdayLastName() + " "
                    + birthday.get().getBirthdayDate();

            birthdayRepository.deleteById(Long.parseLong(birthdayId));
            buildMessage(chatId, answer);
            log.info(answer);

        }
        clearSelectedcommend();
    }
    private void deleteThisTermin(long chatId, String terminId) {
        if (terminRepository.findById(Long.parseLong(terminId)).isPresent()){
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

        List<String> parser =List.of(parseString.split(", "));

        for (String termStrId : parser) {
            Long terminId = Long.valueOf(termStrId);

            System.out.println(terminId);
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

        List<String> parser =List.of(parseString.split(", "));

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

        List<String> parser =List.of(parseString.split(", "));

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

    //UserFunk
    private void myDataUser(long chatId) {
        String answer ="";
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            List<Termin> termins = terminRepository.findByUserId(chatId);
            List<Birthday> birthdays = birthdayRepository.findByUserId(chatId);
            List<Reminder> reminders = reminderRepository.findByUserId(chatId);


            String id = String.valueOf(user.get().getId());
            String userNameInfo = user.get().getUserName() == null ? "your username null, please set you /username":
                    user.get().getUserName().equalsIgnoreCase("null") ? "your username null, please set you /username":
                    "user: " + user.get().getUserName() + " | Edit /username";
            String firstNameInfo = user.get().getFirstName() == null ? "your Firstname null, please set you /firstname":
                    user.get().getFirstName().equalsIgnoreCase("null") ? "your Firstname null, please set you /firstname":
                    "Firstname: " + user.get().getFirstName() + " | Edit /firstname";
            String lastNameInfo = user.get().getLastName() == null ? "your lastname null, please set you /lastname":
                    user.get().getLastName().equalsIgnoreCase("null") ? "your lastname null, please set you /lastname":
                    "Lastname: " + user.get().getLastName() + " | Edit /lastname";


            String terminInfo = !termins.isEmpty() ? "You have saved  Termins: " + termins.size() + " \n" + "show all Termins /showtermin \n" :
                    "You have not Termins, ADD  /newtermin \n";
            String birthdayInfo = !birthdays.isEmpty() ? "You have saved  Birthdays: " +  birthdays.size() + " \n" + "show all Birthdays /showbirthday "+"\n" :
                    "You have not Birthdays, ADD  /newbirthday \n";
            String reminderInfo = !reminders.isEmpty() ? "You have saved  Reminders: " +  reminders.size() + " \n" + "show all Reminders /showreminder "+"\n" :
                    "You have not Reminders, ADD  /newreminder \n";


            answer= "this is your saved data\n" +
                    "----------------------------" +" \n"+
                    id + " \n" +
                    userNameInfo + " \n" +
                    firstNameInfo +" \n"+
                    lastNameInfo +" \n"+

                    "----------------------------" +" \n"+

                    "Region: " + user.get().getRegion() + "\n set you /setregion for your correctly Time" + "\n" +
                    "----------------------------" +" \n"+
                    "registered: " + user.get().getRegisteredAt() +"\n"+
                    "----------------------------" +" \n"+
                    terminInfo+birthdayInfo+reminderInfo+
                    "----------------------------" +" \n"+
                    "\n" +
                    "if you want to delete click /deleteuserdata";

            log.info("Show UserData: " + user.get().getUserName()
                    + ", " + user.get().getFirstName()
                    + ", " + user.get().getLastName()
                    + ", Region: " + user.get().getRegion()
                    + ", Registered: " + user.get().getRegisteredAt()
                    + ", User have saved Termins: " + termins.size() +"\n"
                    + ", User have saved Birthdays: " + birthdays.size() +"\n"
                    + ", User have saved Reminders: " + reminders.size() +"\n"
            );
        }else {
            answer="you are not in our database yet,\n" +
                    "if you want to register, then click /registrade ";
        }
        buildMessage(chatId, answer);
    }
    private void deleteUserData(Long chatId, String messageText){

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
        }else if (!containsDigitsRegex(messageText)){
            buildMessage(chatId, "\"you entered the wrong numbers\"");
        }else {
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
        if (userRepository.findById(chatId).isPresent()){
            Optional<User> user;
            user= Optional.of(userRepository.findById(chatId).get());
            log.info("Delete User: " + user.get().getUserName()
                    +", "+user.get().getFirstName()
                    +", "+user.get().getLastName()
                    +", Registered: "+user.get().getRegisteredAt());
            String answer ="your data has been deleted from the database "+ user.get().getUserName();
            userRepository.deleteById(chatId);
            buildMessage(chatId, answer);
        }
    }
    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = User.builder()
                    .id(chatId)
                    .firstName(chat.getFirstName())
                    .lastName(chat.getLastName())
                    .userName(chat.getUserName())
                    .region("null")
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("User save: " + user);
            buildMessage(chatId, "User save: " + user.getUserName());
        }
    }
    private void setUsername(long chatId, Message message) {
        Optional<User> user = userRepository.findById(chatId);
        String userName =message.getText();
        if (!userName.isEmpty()) {
            user.get().setUserName(userName);
            userRepository.save(user.get());

            String answer = "User: " +user.get().getUserName() + " set Username: " + userName;
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

            String answer = "User: " +user.get().getUserName() + " set Firstname: " + firstName;
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

            String answer = "User: " +user.get().getUserName() + " set Lastname: " + lastName;
            buildMessage(chatId, answer);
            log.info(answer);

        }
        myDataUser(chatId);
        clearSelectedcommend();
    }
     private void setUserRegion(Message message, long chatId) {
        Optional<User> user = userRepository.findById(chatId);
        String region = message.getText();
        if (!region.isEmpty()) {
            user.get().setRegion(region);
            userRepository.save(user.get());

            String answer = "User: " +user.get().getUserName() + " set region: " + region;
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
    private void start(long chatId ){

        Optional<User> user = userRepository.findById(chatId);

        String userName = user.get().getUserName();
        String setRegion = user.get().getRegion().equalsIgnoreCase("null") ?
                "Please set you Region for correctly time! /setregion" : "";

        String answer = user.get().getUserName().equalsIgnoreCase("null") ? "HI, your username is null, please set you /username" + setRegion :
                "Hi. " + userName + " nice to meet you.\n"+setRegion;

        log.info("Replied to user: " + userName);
        buildMessage(chatId,answer);
    }
    private void defaultCo(long chatId, String userName, String messageText){
        String answer = "Sorry, command was not recognized";
        log.info("Replied to user: " + userName + " user falls command " + messageText);
        buildMessage(chatId,answer);
    }



    @Scheduled(cron = ("* */1 * * * *"))
    private void reminderSender(){
        List<Reminder> allReminders = reminderRepository.findAll();
        List<Integer> reminderDays = new ArrayList<>();

        if (!allReminders.isEmpty()){
            for (Reminder reminder : allReminders){

                LocalDate localDate = LocalDate.now();
                LocalTime localTime = LocalTimeNow(reminder.getUser().getId());

                List<String> days = List.of(reminder.getReminderDays().split(" "));
                for (String day : days){
                    reminderDays.add(Integer.parseInt(day));
                }

                List<String>timeParse=List.of(reminder.getReminderTime().split(":"));
                List<String> minusMinSort = buildReminderMinuts(reminder);


                LocalTime reminderTime = LocalTime.of(Integer.parseInt(timeParse.get(0)), Integer.parseInt(timeParse.get(1)));

                int weekDayNow = localDate.getDayOfWeek().getValue();

                sendReminder(reminder, minusMinSort, reminderTime, reminderDays, weekDayNow, localTime);

                reminderDays.clear();

            }
        }
    }

    private static List<String> buildReminderMinuts(Reminder reminder) {
        List<String> minusMinEdit =new ArrayList<>();
        List<String> minusMinEditTrim =new ArrayList<>();
        List<String> minusMinEditAddNull =new ArrayList<>();
        String reminderMinusMin;

        if (reminder.getReminderMinusMin()!=null){
            reminderMinusMin= reminder.getReminderMinusMin();
        }else {
            reminderMinusMin="05 10 15";
        }

        if (reminderMinusMin.contains(" ")) {
            minusMinEdit = List.of(reminderMinusMin.split(" "));

        }
        if (reminderMinusMin.contains(", ")){
            minusMinEdit = List.of(reminderMinusMin.split(", "));
        }
        if (reminderMinusMin.contains(",")){
            minusMinEdit = List.of(reminderMinusMin.split(","));
        }
        if (reminderMinusMin.contains(".")){
            minusMinEdit = List.of(reminderMinusMin.split("\\."));
        }
        if (reminderMinusMin.length() == 1){
            minusMinEdit.add(reminderMinusMin);
        }

        for (String s : minusMinEdit){
            minusMinEditTrim.add(s.trim());
        }

        for (String s : minusMinEditTrim){
            if (s.length() ==1){
                minusMinEditAddNull.add("0"+s);
            }else {
                minusMinEditAddNull.add(s);
            }
        }
        List<String>minusMinSortEdit=minusMinEditAddNull.stream().sorted(Comparator.reverseOrder()).toList();

        List<String>minusMinSort = new ArrayList<>();

        for (String s : minusMinSortEdit){

            if (s.length()>=2) {
                String x = s.substring(0, 1);
                if (x.equals("0")) {
                    minusMinSort.add(s.substring(1));
                } else {
                    minusMinSort.add(s);
                }
            }else if (s.length() ==1){
                minusMinSort.add(s);
            }
        }


        return minusMinSort;
    }

    private void sendReminder(Reminder reminder, List<String> minusMinSort, LocalTime reminderTime, List<Integer> reminderDays, int weekDayNow, LocalTime localTime) {
        int count = 0;

        for (int i = 0; i < minusMinSort.size(); i++) {
            LocalTime reminderTimeMin = null;
            reminderTimeMin = minusMinSort.get(i).equals("0") ? reminderTime : reminderTime.minusMinutes(Integer.parseInt(
                    minusMinSort.get(i)));
            if (
                    reminderDays.contains(weekDayNow)
                            && ChronoUnit.HOURS.between(localTime, reminderTimeMin) == 0
                            && ChronoUnit.MINUTES.between(localTime, reminderTimeMin) == 0
                            && ChronoUnit.SECONDS.between(localTime, reminderTimeMin) == 0
                            && !(ChronoUnit.SECONDS.between(reminderTime, reminderTimeMin) == 0)
            ) {
                Long chatId = reminder.getUser().getId();

                String answer = "In " + minusMinSort.get(i) + " minutes it will be | " + reminder.getReminderTittle() + " | at " + reminder.getReminderTime() + " o'clock.";

                buildMessage(chatId, answer);
                log.info(answer);
            }
            count++;
        }
        if (minusMinSort.isEmpty() || minusMinSort.get(0).equals("0") || minusMinSort.size() == count){
            if (
                reminderDays.contains(weekDayNow)
                        && ChronoUnit.HOURS.between(localTime, reminderTime) ==0
                        && ChronoUnit.MINUTES.between(localTime, reminderTime) ==0
                        && ChronoUnit.SECONDS.between(localTime, reminderTime) ==0
            ) {
                Long chatId = reminder.getUser().getId();
                String answer = "Now " + reminder.getReminderTittle() + " " + reminder.getReminderTime();

                buildMessage(chatId, answer);
                log.info(answer);

            }
        }

    }

    @Scheduled(cron = ("* */1 * * * *"))
    private void terminSender() {

        List<Termin> allTermins = terminRepository.findAll();
        if (!allTermins.isEmpty()){
            for (Termin termin : allTermins){

                LocalDate localDate = LocalDate.now();
                LocalTime localTime =  LocalTimeNow(termin.getUser().getId());

                List<String>dateParse=List.of(termin.getTerminDate().split("\\."));
                List<String>timeParse=List.of(termin.getTerminTime().split(":"));

                LocalDate terminDate = LocalDate.of(Integer.parseInt(dateParse.get(2)), Integer.parseInt(dateParse.get(1)), Integer.parseInt(dateParse.get(0)));
                LocalTime terminTime = LocalTime.of(Integer.parseInt(timeParse.get(0)), Integer.parseInt(timeParse.get(1)));

                List<String> minusMinSort = buildTerminMinuts(termin);

                sendTermin(termin, minusMinSort, terminTime, localDate, terminDate, localTime);
            }
        }
    }
    private List<String> buildTerminMinuts(Termin termin) {

        List<String> minusMinEdit =new ArrayList<>();
        List<String> minusMinEditTrim =new ArrayList<>();
        List<String> minusMinEditAddNull =new ArrayList<>();
        String terminMinusMin;

        if (termin.getTerminMinusMin()!=null){
            terminMinusMin= termin.getTerminMinusMin();
        }else {
            terminMinusMin="05 10 15";
        }

        if (terminMinusMin.contains(" ")) {
            minusMinEdit = List.of(terminMinusMin.split(" "));

        }
        if (terminMinusMin.contains(", ")){
            minusMinEdit = List.of(terminMinusMin.split(", "));
        }
        if (terminMinusMin.contains(",")){
            minusMinEdit = List.of(terminMinusMin.split(","));
        }
        if (terminMinusMin.contains(".")){
            minusMinEdit = List.of(terminMinusMin.split("\\."));
        }
        if (terminMinusMin.length() == 1){
            minusMinEdit.add(terminMinusMin);
        }

        for (String s : minusMinEdit){
            minusMinEditTrim.add(s.trim());
        }

        for (String s : minusMinEditTrim){
            if (s.length() ==1){
                minusMinEditAddNull.add("0"+s);
            }else {
                minusMinEditAddNull.add(s);
            }
        }
        List<String>minusMinSortEdit=minusMinEditAddNull.stream().sorted(Comparator.reverseOrder()).toList();

        List<String>minusMinSort = new ArrayList<>();

        for (String s : minusMinSortEdit){

            if (s.length()>=2) {
                String x = s.substring(0, 1);
                if (x.equals("0")) {
                    minusMinSort.add(s.substring(1));
                } else {
                    minusMinSort.add(s);
                }
            }else if (s.length() ==1){
                minusMinSort.add(s);
            }
        }


        return minusMinSort;
    }
    private void sendTermin(Termin termin, List<String> minusMinSort, LocalTime terminTime, LocalDate localDate, LocalDate terminDate, LocalTime localTime) {
        int count = 0;

        for (int i = 0; i < minusMinSort.size(); i++) {
            LocalTime terminTimeMin = minusMinSort.get(i).equals("0") ? terminTime : terminTime.minusMinutes(Integer.parseInt(
                    minusMinSort.get(i)));

            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0
                            && ChronoUnit.HOURS.between(localTime, terminTimeMin) == 0
                            && ChronoUnit.MINUTES.between(localTime, terminTimeMin) == 0
                            && ChronoUnit.SECONDS.between(localTime, terminTimeMin) == 0
                            && !(ChronoUnit.SECONDS.between(terminTime, terminTimeMin) ==0)
            ) {
                Long chatId = termin.getUser().getId();
                String answer = termin.getTerminName() + "\nIn " + minusMinSort.get(i) + " minutes it will be" +  "\nat " + termin.getTerminTime() + " o'clock\nDate: "+ termin.getTerminDate();

                buildMessage(chatId, answer);
                log.info(answer);
            }
            count++;
        }

        if (minusMinSort.isEmpty() || minusMinSort.get(0).equals("0") || minusMinSort.size() == count) {
            if (
                    ChronoUnit.DAYS.between(localDate, terminDate) == 0
                            && ChronoUnit.HOURS.between(localTime, terminTime) == 0
                            && ChronoUnit.MINUTES.between(localTime, terminTime) == 0
                            && ChronoUnit.SECONDS.between(localTime, terminTime) == 0
            ) {
                Long chatId = termin.getUser().getId();
                String answer =  termin.getTerminName() + "\nNow \n"+ "at " + termin.getTerminTime() + " o'clock\nDate: "+ termin.getTerminDate();

                buildMessage(chatId, answer);
                log.info(answer);
            }
        }
    }


    @Scheduled(cron = "0 00 19 * * *")
    private void birthdaySender1(){
        buildBirthdayScheduler("Tomorrow is ", 1);
    }
    @Scheduled(cron = "0 00 08 * * *")
    private void birthdaySender2(){
        buildBirthdayScheduler("Today is ", 0);
    }
    @Scheduled(cron = "0 00 15 * * *")
    private void birthdaySender3(){
        buildBirthdayScheduler("Today is ", 0);
    }

    private void buildBirthdayScheduler(String sendMsg, long minusDay) {
        List<Birthday> allbirthdays = birthdayRepository.findAll();
        if (!allbirthdays.isEmpty()) {
            for (Birthday birthday : allbirthdays) {
                LocalDate localDate = LocalDate.now();

                List<String>dateParse=List.of(birthday.getBirthdayDate().split("\\."));
                LocalDate birthdayDate = LocalDate.of(Integer.parseInt(dateParse.get(2)), Integer.parseInt(dateParse.get(1)), Integer.parseInt(dateParse.get(0)));
                LocalDate expactedDay = birthdayDate.minusDays(minusDay);

                if (Period.between(localDate, expactedDay).getMonths()==0
                        && Period.between(localDate, expactedDay).getDays() == 0
                ){
                    Long chatId = birthday.getUser().getId();
                    String answer = sendMsg + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLastName() + " birthday |  " + birthday.getBirthdayDate();
                    buildMessage(chatId, answer);
                    log.info(answer);
                }
            }
        }
    }
    private static String dateParser(String datapar, String[] terminDateparser) {

        String [] dateParseSplit = datapar.split(" ");

        String [] date = dateParseSplit[0].split("-");

        String yearEX = date[0];

        String yearDopp ="20";

        String yEN = yearEX.substring(2, 4);
        int yENInt = Integer.parseInt(yEN);

        String yearEditAdd20 = yearEX.substring(0, 2);

        int checkYear19 = Integer.parseInt(yearEditAdd20)-1;
        String yearEditAdd19 = Integer.toString(checkYear19);

        String day = terminDateparser[0].trim();
        String month = terminDateparser[1].trim();
        String yearEdit = terminDateparser[2].trim();

        int yE = Integer.parseInt(yearEdit);

        if (yE > yENInt){
            yearDopp=yearEditAdd19;
        }else {
            yearDopp=yearEditAdd20;
        }

        if (selectedCommands.contains("/newtermin")){yearDopp=yearEditAdd20;}

        String year = yearEdit.length()==2 ? yearDopp+yearEdit :
                yearEdit.length()==4 ? yearEdit : yearEX;

        String setDate = day+"."+month+"."+year;
        return setDate;
    }

    //Date
    private LocalTime LocalTimeNow(Long chatId){
        Map<String, Integer> regions = getRegionsMap();

        Optional<User> user = userRepository.findById(chatId);


        if (regions.containsKey(user.get().getRegion())){
            LocalTime res1 = LocalTime.now().plusHours(regions.get(user.get().getRegion()));
            return res1;
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
                &&Period.between(localDate, november).getMonths() == 0
                && Period.between(localDate, november).getDays() == 0){
            russianHours = 2;
        }else if (Period.between(localDate, march).getYears()==0
                && Period.between(localDate, march).getMonths()==0
                && Period.between(localDate, march).getDays()==0){
            russianHours = 1;
        }
        return russianHours;
    }
    public static int getLetsSunday(int years, int month) {
        int year = years; // Замените на нужный год

        // Создание объекта Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
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
    public void buildMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        sendMessage(sendMessage);
    }
    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        }
        catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }
    }
    private static void clearSelectedcommend() {
        selectedCommands.clear();
        entityId.clear();
    }

}

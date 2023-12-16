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



    public Bot(BotConfig config) {

        this.config = config;

        //Меню Комманд
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get my Data storage"));
        listOfCommands.add(new BotCommand("/setregion", "set your region"));
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
        }catch (TelegramApiException e){
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
    static List<String>selectedCommands = new ArrayList<>();

    @Override
    public void onUpdateReceived(Update update) {
           if (update.hasMessage() && update.getMessage().hasText()){
            handleMessage(update.getMessage());
        }
    }
    private void handleMessage(Message message) {


        String messageTextCheck = message.getText();
        String messageText = "";
        if (messageTextCheck.contains("@TjapkaTerminsager88bot")){
            int i = messageTextCheck.indexOf("@");
            messageText = messageTextCheck.substring(0, i);

        }else {messageText = messageTextCheck;}
        long chatId = message.getChatId();
        String userName = message.getFrom().getUserName();
        String fnkBTN="";


        //------------------------------------------------
        List<String>fnkStart=List.of("/start", "/help");
        List<String>fnkUserQ=List.of("/setregion", "/deleteuserdata");
        List<String>fnkUser= new ArrayList<>(List.of("/registrade", "/mydata"));
        fnkUser.addAll(fnkUserQ);
        List<String>fnkTermin=List.of("/newtermin", "/showtermin", "/deletetermin");
        List<String>fnkBirthDay=List.of("/newbirthday", "/showbirthday", "/deletebirthday");
        List<String>fnkReminder=List.of("/newreminder", "/showreminder", "/deletereminder");
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
        //------------------------------------------------
        if (fnkStart.contains(fnkBTN)) {
            startFunk(message, fnkBTN, chatId, userName);
            selectedCommands.clear();

        }else if (fnkUser.contains(fnkBTN)){
            userFunk(message, fnkBTN, chatId, userName);
            if (!fnkUserQ.contains(fnkBTN)) {
                selectedCommands.clear();
            }

        }else if (fnkBirthDay.contains(fnkBTN)){
            birthDayFunk(message, fnkBTN, chatId, userName);

        }else if (fnkReminder.contains(fnkBTN)){
            reminderFunk(message, fnkBTN, chatId, userName);

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
            deleteOneTermin(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newbirthday")){
            buildNewBirthday(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletebirthday")){
            deleteOneBirthday(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/newreminder")){
            buildNewReminder(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deletereminder")){
            deleteOneReminder(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/setregion")){
            setUserRegion(message, chatId);
        }else if (!messageText.contains("/") && selectedCommands.get(0).equals("/deleteuserdata")){
            deleteUserData(chatId, messageText);
        }
        //------------------------------------------------
    }

    //Menus

    private void startFunk(Message message, String messageText, long chatId, String userName) {
        switch (messageText) {
            case "/start":
                registerUser(message);
                start(chatId, userName);
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
            case "/mydata":
                myDataUser(chatId);
                break;
            case "/setregion":
                buildMessage(chatId, "please set your region " +
                        "\nRussia" +
                        "\nGermany" +
                        "\nUkraine");
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
            case "/deletetermin":
                showAllTermins(chatId);
                buildMessage(chatId, "Please enter your terminID Of delete");
                break;
            default:
                defaultCo(chatId, userName, fnkBTN);
                break;
        }
    }
    private void birthDayFunk(Message message, String messageText, long chatId, String userName) {
        switch (messageText) {
            case "/newbirthday":
                buildMessage(chatId, "Please enter new Birthday! \nExample:  \nPerson Firstname, Person Lastname, Birthday Date\nExample Firstname, Example Lastname, 00.00.0000");
                break;
            case "/showbirthday":
                showAllBirthday(chatId);
                break;
            case "/deletebirthday":
                showAllBirthday(chatId);
                buildMessage(chatId, "Please enter your BirthDayID Of delete");
                break;
            default:
                defaultCo(chatId, userName, messageText);
                break;

        }
    }
    private void reminderFunk(Message message, String messageText, long chatId, String userName) {
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
                showAllReminder(chatId);
                buildMessage(chatId, "Please enter your ReminderID Of delete");
                break;
            default:
                defaultCo(chatId, userName, messageText);
                break;

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
                    .user(user.get())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();

            reminderRepository.save(reminder);

            String answer = user.get().getUserName() + " | Reminder saved: " + reminderTittle + " " + reminderDays + " " + reminderTime;
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void showAllReminder(long chatId) {
        List<Reminder> userReminder = reminderRepository.findByUserId(chatId);
        String answer="";
        if (!userReminder.isEmpty()) {
            for (Reminder reminder : userReminder) {

                String createdDate = reminder.getCreatedAt().toString();

                List<String>parser = List.of(createdDate.split(" "));

                String date =parser.get(0);
                List<String>dateList=List.of(date.split("-"));


                answer = reminder.getId() + ": " + reminder.getReminderTittle() + " " + reminder.getReminderDays() + " | " +
                        reminder.getReminderTime() + " | created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) ;

                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Reminders: "+userReminder.size());
        }else {
            answer ="you have not Reminders";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void deleteOneReminder(Message message, long chatId) {
        String parseString = message.getText();

        Long reminderId = Long.valueOf(parseString);

        Optional<Reminder> reminder = reminderRepository.findById(reminderId);

        String answer="deleted Reminder: " + reminder.get().getId() + " " + reminder.get().getReminderTittle() + " " + reminder.get().getReminderTime();

        reminderRepository.deleteById(reminderId);
        buildMessage(chatId, answer);
        log.info(answer);
        selectedCommands.clear();
    }
    //Birthday
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
        buildMessage(chatId, answer);
        addReminder(chatId, reminderTittle, reminderDays, reminderTime);
        selectedCommands.clear();
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
        buildMessage(chatId, answer);
        addBirthday(chatId, firstName, lastName, setDate);
        selectedCommands.clear();
    }
    private void addBirthday(long chatId, String firstName, String lastName, String setDate) {
        if (userRepository.findById(chatId).isPresent()) {
            Optional<User> user;
            user = Optional.of(userRepository.findById(chatId).get());
            Birthday birthday = Birthday.builder()
                    .birthdayFirstName(firstName)
                    .birthdayLarstName(lastName)
                    .birthdayDate(setDate)
                    .user(user.get())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            birthdayRepository.save(birthday);

            String answer = user.get().getUserName() + " | Birthday saved: " + firstName + " " + lastName + " " + setDate;
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void showAllBirthday(long chatId) {
        List<Birthday> userBirthdays = birthdayRepository.findByUserId(chatId);
        String answer="";
        if (!userBirthdays.isEmpty()) {
            for (Birthday birthday : userBirthdays) {

                String createdDate = birthday.getCreatedAt().toString();

                List<String>parser = List.of(createdDate.split(" "));

                String date =parser.get(0);
                List<String>dateList=List.of(date.split("-"));


                answer = birthday.getId() + ": " + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLarstName() + " | " +
                        birthday.getBirthdayDate() + " | created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) ;

                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Birthdays: "+userBirthdays.size());
        }else {
            answer ="you have not Birthdays";
            log.info(answer);
            buildMessage(chatId, answer);
        }
    }
    private void deleteOneBirthday(Message message, long chatId) {
        String parseString = message.getText();

        Long birthdayId = Long.valueOf(parseString);

        Optional<Birthday> birthday = birthdayRepository.findById(birthdayId);

        String answer="deleted Birthday: " + birthday.get().getId() + " " + birthday.get().getBirthdayFirstName() + " " + birthday.get().getBirthdayLarstName();

        birthdayRepository.deleteById(birthdayId);
        buildMessage(chatId, answer);
        log.info(answer);
        selectedCommands.clear();
    }
    //TerminFunk
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
        buildMessage(chatId, answer);
        addTermin(chatId, terminName, setDate, terminTime);
        selectedCommands.clear();
    }
    private void showAllTermins(long chatId) {
        List<Termin> userTermins = terminRepository.findByUserId(chatId);
        String answer="";
        if (!userTermins.isEmpty()) {
            for (Termin termin : userTermins) {

                String createdDate = termin.getCreatedAt().toString();
                List<String>parser = List.of(createdDate.split(" "));

                String date =parser.get(0);
                List<String>dateList=List.of(date.split("-"));

                String time = parser.get(1);
                List<String>timeList=List.of(time.split(":"));

                answer = termin.getId() + ": " + termin.getTerminName() + " | " +
                        termin.getTerminDate() + " | " + termin.getTerminTime() + " | created: " + dateList.get(2)+"."+ dateList.get(1)+"."+ dateList.get(0) + " | " +
                        timeList.get(0)+":"+timeList.get(1);


                buildMessage(chatId, answer);
            }
            log.info("User show your List of all Termins: "+userTermins.size());
        }else {
            answer ="you have not Termins";
            log.info(answer);
            buildMessage(chatId, answer);
        }
        }
    private void deleteOneTermin(Message message, Long chatId) {

        String parseString = message.getText();

        Long terminId = Long.valueOf(parseString);

        System.out.println(terminId);
        Optional<Termin> termin = terminRepository.findById(terminId);

        String answer="deleted termin: " + termin.get().getId() + " " + termin.get().getTerminName();

        terminRepository.deleteById(terminId);
        buildMessage(chatId, answer);
        log.info(answer);
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
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            terminRepository.save(termin);

            String answer = user.get().getUserName() + " Termin saved: " + terminName;
            log.info(answer);
            buildMessage(chatId, answer);
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
            answer= "this is your saved data\n" +
                    "user: " + user.get().getUserName() +"\n" +
                    "Firstname: " + user.get().getFirstName() +"\n"+
                    "Lastname: " + user.get().getLastName() +"\n" +
                    "Region: " + user.get().getRegion() +"\n" +
                    "registered: " + user.get().getRegisteredAt() +"\n" +
                    "You have saved  Termins: " +  termins.size() + " \n" + "show all Termins /showtermin "+"\n" +
                    "You have saved  Birthdays: " +  birthdays.size() + " \n" + "show all Birthdays /showbirthday "+"\n" +
                    "You have saved  Reminders: " +  reminders.size() + " \n" + "show all Reminders /showreminder "+"\n" +

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
                    .registeredAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            userRepository.save(user);
            log.info("User save: " + user);
            buildMessage(chatId, "User save: " + user.getUserName());
        }
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

        selectedCommands.clear();
    }
    //StartFunk
    private void help(long chatId) {
        log.info("Replied to user: ");
        buildMessage(chatId, HELP_TEXT_START);
    }
    private void start(long chatId, String userName){
        String answer = "Hi. " + userName + " nice to meet you";
        log.info("Replied to user: " + userName);
        buildMessage(chatId,answer);
    }
    private void defaultCo(long chatId, String userName, String messageText){
        String answer = "Sorry, command was not recognized";
        log.info("Replied to user: " + userName + " user falls command " + messageText);
        buildMessage(chatId,answer);
    }



   // @Scheduled(cron = ("* */1 * * * *"))
    private void reminderSender(){
        List<Reminder> allReminders = reminderRepository.findAll();
        List<Integer> reminderDays = new ArrayList<>();

        if (!allReminders.isEmpty()){
            for (Reminder reminder : allReminders){

                LocalDate localDate = LocalDate.now();
                LocalTime localTime = LocalTimeNow(reminder.getUser().getId());

                System.out.println(localTime.getHour());

                List<String> days = List.of(reminder.getReminderDays().split(" "));
                for (String day : days){
                    reminderDays.add(Integer.parseInt(day));
                }


                List<String>timeParse=List.of(reminder.getReminderTime().split(":"));

                LocalTime reminderTime = LocalTime.of(Integer.parseInt(timeParse.get(0)), Integer.parseInt(timeParse.get(1)));
                LocalTime reminderTimeMin5 = reminderTime.minusMinutes(5);
                LocalTime reminderTimeMin10 = reminderTime.minusMinutes(10);
                LocalTime reminderTimeMin15 = reminderTime.minusMinutes(15);

                int weekDayNow = localDate.getDayOfWeek().getValue() + 1;

                if (
                        reminderDays.contains(weekDayNow)
                     && ChronoUnit.HOURS.between(localTime, reminderTimeMin15) ==0
                     && ChronoUnit.MINUTES.between(localTime, reminderTimeMin15) ==0
                     && ChronoUnit.SECONDS.between(localTime, reminderTimeMin15) ==0
                ){
                    Long chatId = reminder.getUser().getId();
                    String answer = "Min -15 " + reminder.getReminderTittle() + " " + reminder.getReminderTime();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }else if (
                        reminderDays.contains(weekDayNow)
                        && ChronoUnit.HOURS.between(localTime, reminderTimeMin10) ==0
                        && ChronoUnit.MINUTES.between(localTime, reminderTimeMin10) ==0
                        && ChronoUnit.SECONDS.between(localTime, reminderTimeMin10) ==0
                ){
                    Long chatId = reminder.getUser().getId();
                    String answer = "Min -10 " + reminder.getReminderTittle() + " " + reminder.getReminderTime();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }else if (
                        reminderDays.contains(weekDayNow)
                        && ChronoUnit.HOURS.between(localTime, reminderTimeMin5) ==0
                        && ChronoUnit.MINUTES.between(localTime, reminderTimeMin5) ==0
                        && ChronoUnit.SECONDS.between(localTime, reminderTimeMin5) ==0

                ){
                    Long chatId = reminder.getUser().getId();
                    String answer = "Min -5 " + reminder.getReminderTittle() + " " + reminder.getReminderTime();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }

                else if (
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
    }

    // @Scheduled(cron = ("* */1 * * * *"))

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

                LocalTime terminTimeMin10 = terminTime.minusMinutes(10);
                LocalTime terminTimeMin5 = terminTime.minusMinutes(5);


                if (
                        ChronoUnit.DAYS.between(localDate, terminDate) == 0
                                && ChronoUnit.HOURS.between(localTime, terminTimeMin10) == 0
                                && ChronoUnit.MINUTES.between(localTime, terminTimeMin10) == 0
                                && ChronoUnit.SECONDS.between(localTime, terminTimeMin10) == 0
                ) {
                    Long chatId = termin.getUser().getId();
                    String answer = "Min -10 " + termin.getTerminName() + " " + termin.getTerminDate();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }else if (
                        ChronoUnit.DAYS.between(localDate, terminDate) == 0
                                && ChronoUnit.HOURS.between(localTime, terminTimeMin5) == 0
                                && ChronoUnit.MINUTES.between(localTime, terminTimeMin5) == 0
                                && ChronoUnit.SECONDS.between(localTime, terminTimeMin5) == 0
                ) {
                    Long chatId = termin.getUser().getId();
                    String answer = "Min -5 " + termin.getTerminName() + " " + termin.getTerminDate();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }else if (
                        ChronoUnit.DAYS.between(localDate, terminDate) == 0
                                && ChronoUnit.HOURS.between(localTime, terminTime) == 0
                                && ChronoUnit.MINUTES.between(localTime, terminTime) == 0
                                && ChronoUnit.SECONDS.between(localTime, terminTime) == 0
                ) {
                    Long chatId = termin.getUser().getId();
                    String answer = "Min 00 " + termin.getTerminName() + " " + termin.getTerminDate();

                    buildMessage(chatId, answer);
                    log.info(answer);
                }
            }
        }
    }
   // @Scheduled(cron = "0 00 19 * * *")

    private void birthdaySender1(){
        buildBirthdayScheduler("Tomorrow is ", 1);
    }
    //@Scheduled(cron = "0 00 08 * * *")

    private void birthdaySender2(){
        buildBirthdayScheduler("Today is ", 0);
    }
    //@Scheduled(cron = "0 00 15 * * *")

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
                    String answer = sendMsg + birthday.getBirthdayFirstName() + " " + birthday.getBirthdayLarstName() + " birthday |  " + birthday.getBirthdayDate();
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
        regions.put("Ukraina", -1);
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
}

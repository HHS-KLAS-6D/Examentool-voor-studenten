import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class JSON {

    // Maakt van een Object uit een .json file een int.
    public static int toInt(Object input) {
        try {
            return (int) (long) input;
        } catch (Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    // Leest complete .json file uit en returned dit als een array.
    public static JSONArray readFile(String path) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/main/resources/" + path + ".json")) {
            Object obj = jsonParser.parse(reader);
            JSONArray json = (JSONArray) obj;

            return json;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    //Write to the json file
    public static void writeJSON(JSONArray writeContent, String jsonFileName) {
        try (FileWriter file = new FileWriter("src/main/resources/" + jsonFileName + ".json")) {
            file.write(writeContent.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get alle studenten die een examen hebben gemaakt. Als input geef je het unieke examenID uit examens.json
    public static ArrayList<Student> getStudenten(int examenId) {
        JSONArray studenten = readFile("studenten"); // Roept methode readFile aan die de hele file met studenten teruggeeft als array.
        ArrayList<Student> lijstMetStudenten = new ArrayList<>(); //In deze lijst met studenten worden alle studenten toegevoegd die een bepaald examen hebben gemaakt.

        //Gaat alle studenten langs.
        for (Object student : studenten) {

            boolean isToegevoegd = false;

            //Maak van het Object student een JSON Object
            JSONObject jsonObject = (JSONObject) student;
            JSONArray gemaakteExamens = (JSONArray) jsonObject.get("examens");

            for (Object gemaakteExamenObject : gemaakteExamens) {
                JSONObject gemaakteExamen = (JSONObject) (gemaakteExamenObject);

                int examenNummer = toInt(gemaakteExamen.get("examenID"));

                //Als het meegegeven examenId gelijk is aan een examenNummer in de array in de student, dan wordt deze student toegevoegd aan de ArrayList "lijstMetStudenten".
                // Op deze manier krijg je in die ArrayList dus een overzicht van alle studenten die een bepaalde meegegeven toets hebben gemaakt.
                if (examenNummer == examenId && !isToegevoegd) {
                    isToegevoegd = true;
                    lijstMetStudenten.add(new Student(jsonObject.get("naam").toString(), jsonObject.get("achternaam").toString(), toInt(jsonObject.get("nummer")), toInt(jsonObject.get("gehaaldeExamens"))));
                }
            }
        }
        return lijstMetStudenten;
    }

    //returned alle gemaakte examens door een student. Geef als input het studentnummer uit de file studenten.json
    public static ArrayList<Examen> getExamens(int studentNummer) {
        JSONArray studenten = readFile("studenten");
        ArrayList<Examen> lijstMetExamens = new ArrayList<>();

        for (Object student : studenten) {
            JSONObject studentObject = (JSONObject) student;
            int studentId = toInt(studentObject.get("nummer"));

            //check of het nummer uit de json file gelijk staat aan het meegegeven nummer uit de parameter
            if (studentId == studentNummer) {

                //get de lisjt van gemaakte examens door de student
                JSONArray gemaakteExamens = (JSONArray) studentObject.get("examens");

                for (Object gemaakteExamenObject : gemaakteExamens) {
                    JSONObject gemaakteExamen = (JSONObject) (gemaakteExamenObject);

                    //get examen Object met het unieke ID
                    JSONObject examenObject = getExamen(toInt(gemaakteExamen.get("examenID")));

                    lijstMetExamens.add(new Examen(examenObject.get("naam").toString(), toInt(examenObject.get("id")), toInt(examenObject.get("totaalVragen"))));
                }
            }
        }
        return lijstMetExamens;
    }

    //return data van een student met het studentnummer
    public static JSONObject getStudent(int studentNummer) {
        JSONArray studenten = readFile("studenten");

        for (Object student : studenten) { // Gaat alle studenten in de studenten file langs.
            JSONObject jsonObject = (JSONObject) student;
            int studentId = toInt(jsonObject.get("nummer"));

            if (studentId == studentNummer) { // Als je meegegeven studentNummer gelijk is aan een in de file gevonden studentnummer dan geeft hij dat object terug. Dat is tenslotte de student naar wie je zoekt. Je krijgt alle eigenschappen van die student terug.
                return jsonObject;
            }
        }
        return null;
    }

    // Deze methode doet hetzelfde als de methode hierboven, maar dan voor examens en op basis van een examenId.
    public static JSONObject getExamen(int examenId) {
        JSONArray examens = readFile("examens");
        for (Object examen : examens) {
            JSONObject jsonObject = (JSONObject) examen;
            int examenNummer = toInt(jsonObject.get("id"));
            if (examenNummer == examenId) {
                return jsonObject;
            }
        }
        return null;
    }

    //Get data van het gemaakte examen met het unieke ID van dit gemaakte examen. Dit ID staat opgeslagen onder "examens" in student.json
    public static JSONObject getExamenAntwoorden(int uniekId) {
        JSONArray examenAntwoorden = readFile("examenAntwoorden");

        for (Object examenAntwoord : examenAntwoorden) {
            JSONObject jsonObject = (JSONObject) examenAntwoord;
            int id = toInt(jsonObject.get("id"));

            if (id == uniekId) {
                return jsonObject;
            }
        }
        return null;
    }

    //voeg een student toe aan studenten.json met de 3 parameters
    public static void addStudent(String name, String achternaam, String wachtwoord) {
        //generate a random number that will be assigned to the student
        Random rnd = new Random();
        int number = Integer.parseInt(String.format("2022%04d", rnd.nextInt(99999)));

        //read the whole student file
        JSONArray studenten = JSON.readFile("studenten");

        //create a new student json object
        JSONObject newStudent = new JSONObject();
        newStudent.put("naam", name);
        newStudent.put("achternaam", achternaam);
        newStudent.put("nummer", number);
        newStudent.put("wachtwoord", wachtwoord);
        newStudent.put("gehaaldeExamens", 0);
        newStudent.put("gemiddelde", 0.0);
        newStudent.put("examens", new JSONArray());

        //add the newly created json object to the whole list of students
        studenten.add(newStudent);

        writeJSON(studenten, "studenten");
    }

    //verwijder een student met een studentnummer
    public static void removeStudent(int studentNummer) {
        //read the whole student file
        JSONArray studenten = JSON.readFile("studenten");

        JSONArray newStudentList = new JSONArray();

        //loop trough every student in the students json file
        for (Object studentObject : studenten) {
            //create an json object
            JSONObject student = (JSONObject) studentObject;

            //check if the studentJSON number is not equal to the input number
            if (toInt(student.get("nummer")) != studentNummer) {
                newStudentList.add(student);
            }
        }

        writeJSON(newStudentList, "studenten");

        //-------------------------------------------------------------------

        //verwijder alle objects waarin het studentnummer voorkomt in het bestand examenAntwoorden
        JSONArray examenAtwoorden = JSON.readFile("examenAntwoorden");

        JSONArray newExamenAntwoordenList = new JSONArray();

        //loop trough every antwoord in the examenAntwoorden json file
        for (Object antwoordObject : examenAtwoorden) {
            //create an json object
            JSONObject antwoord = (JSONObject) antwoordObject;

            //check if the studentJSON number is not equal to the input number
            if (toInt(antwoord.get("studentNummer")) != studentNummer) {
                newExamenAntwoordenList.add(antwoord);
            }
        }

        writeJSON(newExamenAntwoordenList, "examenAntwoorden");
    }

    public static int saveGemaaktExamen(int examenID, int studentNummer, JSONArray examenVragen) {
        //generate a random number that will be assigned to the students answers
        Random rnd = new Random();
        int number = Integer.parseInt(String.format("%08d", rnd.nextInt(99999999)));

        //read the whole examenAntwoorden file
        JSONArray examenAntwoorden = JSON.readFile("examenAntwoorden");

        //get the exam with its id
        JSONObject gemaakteExamen = getExamen(examenID);

        //get the current date so we can use it in our json object
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        //create a new examAnswers json object
        JSONObject newExamen = new JSONObject();
        newExamen.put("naam", gemaakteExamen.get("naam"));
        newExamen.put("id", number);
        newExamen.put("studentenNummer", studentNummer);
        newExamen.put("date", formatter.format(date));
        newExamen.put("examenID", examenID);
        newExamen.put("totaalVragen", gemaakteExamen.get("totaalVragen"));
        newExamen.put("vragen", examenVragen);


        //EDIT THIS
        newExamen.put("poging", gemaakteExamen.get(0));
        newExamen.put("cijfer", 6.9);


        //add the newly created json object to the whole list of exam answers
        examenAntwoorden.add(newExamen);

        //save the new json file
        writeJSON(examenAntwoorden, "examenAntwoorden");

        return number;
    }

    public static void updateStudent(int studenNummer, Integer antwoordenID, Integer examenID) {
        JSONArray studenten = readFile("studenten");

        JSONObject student = null;

        //find the student object
        for (Object studentObject : studenten) {
            JSONObject tempStudent = (JSONObject) studentObject;

            if (toInt(tempStudent.get("nummer")) == studenNummer) {
                student = tempStudent;
            }
        }

        if (student == null) {
            return;
        }

        //found student
        JSONArray examens = (JSONArray) student.get("examens");

        //maak een nieuwe gemaaktExamen object aan
        JSONObject newExamen = new JSONObject();
        newExamen.put("antwoordID", antwoordenID);
        newExamen.put("examenID", examenID);
        examens.add(newExamen);


        int voldoendeCounter = 0;
        double total = 0.0;
        int count = 0;

        //loop door alle gemaakte examens
        for (Object examenObject : examens) {
            JSONObject examen = (JSONObject) examenObject;

            int num = Integer.parseInt(examen.get("antwoordID").toString());

            JSONObject examenAntwoordObject = getExamenAntwoorden(num);

            total += (double) examenAntwoordObject.get("cijfer");
            count++;

            //+1 als het cijfer groter of gelijk is aan een 5.5
            if ((double) (examenAntwoordObject.get("cijfer")) >= 5.5) {
                voldoendeCounter++;
            }
        }

        //verander de waarde van de 2 attributes
        student.put("gehaaldeExamens", voldoendeCounter);
        student.put("gemiddelde", total / count);

        writeJSON(studenten, "studenten");
    }
}
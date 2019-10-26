package by.htp.command.impl;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import by.htp.command.ICommand;
import by.htp.controller.JspPageName;
import by.htp.controller.RequestParameterName;
import by.htp.entity.Medication;
import by.htp.entity.Patient;
import by.htp.entity.User;
import by.htp.service.PatientService;
import by.htp.service.ServiceException;
import by.htp.service.ServiceProvider;
import by.htp.service.UserService;

public class EditMedicationCommand implements ICommand {

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Patient patient = null;
		String page = null;
		Medication medication = null;
		User user;
		byte[] imageBytes = null;
		int id = 0;
	
		PatientService patientService = ServiceProvider.getInstance().getPatientService();
		UserService userService = ServiceProvider.getInstance().getUserService();
		if (request.getSession() == null) {
			response.sendRedirect(JspPageName.MAIN_PAGE);
		}
		HttpSession session = request.getSession();

		user = (User) session.getAttribute("UserSession");
		patient = (Patient) session.getAttribute("Patient");
		
		int idPatient = patient.getIdPatient();
		medication = new Medication();
			
		String userMedi ;
		
		if ((userMedi= request.getParameter(RequestParameterName.PROCEDURE)) != null ) {
			medication.setProcedure(userMedi);
		} else if ((userMedi=request.getParameter("medication")) != null)  {
			medication.setMedication(userMedi);
		} else if ((userMedi=request.getParameter("operation")) != null) {
			medication.setOperation(userMedi);
		}

		try {
			if (user.getPosition().equals("медсестра")) {
				 id =userService.findUserfromPatient(idPatient);
			} else {
				 id = user.getId();
			}
		
			medication.setIdPatient(idPatient);
			medication.setIdUser(user.getId());
			
			patientService.editMedication(id, medication); 
			medication = patientService.getPatientMedications(patient);
			imageBytes = patientService.getPhoto(patient.getIdPatient());
			
			if (patient != null && patient.getIdPatient() !=0 ) {
								
				request.setAttribute("Patient", patient);
				request.setAttribute("Medications", medication);
				
				if (user.getPosition().equals("медсестра")) {
					page = JspPageName.PATIENT_PAGE_FOR_NURSE;
				} else {
					page = JspPageName.PATIENT_PAGE;
				}
				if (imageBytes != null) {
					String base64Image = Base64.getEncoder().encodeToString(imageBytes);
					request.setAttribute("base64Image", base64Image);
				}
				RequestDispatcher dispatcher = request.getRequestDispatcher(page);
				dispatcher.forward(request, response);
				
				return page;
				
			} else {

			page = JspPageName.USER_PAGE;
				
			RequestDispatcher dispatcher = request.getRequestDispatcher(page);
			dispatcher.forward(request, response);
			}
		} catch (ServiceException e) {
			// TODO 
			e.printStackTrace();
		}
		return page;
	}
	

}

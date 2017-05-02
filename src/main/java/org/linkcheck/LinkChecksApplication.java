package org.linkcheck;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@SpringBootApplication
public class LinkChecksApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinkChecksApplication.class, args);
	}
	@Bean
	CommandLineRunner init(ContactRepository contactRepository) {
		return (evt) -> {
			contactRepository.save(new Contact("John","Doe"));
			contactRepository.save(new Contact("Smith","JK"));
		};
	}
}	

@RestController
@RequestMapping("/contacts")
class ContactController {
	
	   ContactRegistrar contactRegistrar;
	   @Autowired
	   ContactController(ContactRegistrar contactRegistrar) {
		   this.contactRegistrar = contactRegistrar;
	   }
	 
	  @RequestMapping(method=RequestMethod.GET)
	  @ResponseBody 
	  public List<ShortContact> contacts() {
	    List<Contact> contacts = contactRegistrar.findAll();
	    List<ShortContact> resources = new ArrayList<ShortContact>(contacts.size());
	    for(Contact contact : contacts) {
	      ShortContact resource = new ShortContact();
	      resource.setFirstName(contact.getFirstName());
	      resource.setLastName(contact.getLastName());
	      Link detail = linkTo(ContactController.class).slash(contact.getId()).withSelfRel();
	      resource.add(detail);
	      resources.add(resource);
	    }
	    return resources;
	  }

	  @RequestMapping(value="/{id}",method=RequestMethod.GET)
	  public Contact contact(@PathVariable Long id) {		
	    Contact contact = contactRegistrar.findOne(id);
	    return contact;
	  }	 
}

@Component 
@Lazy
class ContactRegistrar {
		
	ContactRepository contactRepository;
		
	@Autowired
	ContactRegistrar(ContactRepository contactRepository){
		this.contactRepository = contactRepository;
	}
		
	List<Contact> findAll() {
	   List<Contact> contacts = contactRepository.findAll();
	   return contacts;
	}
		
	Contact findOne(Long id) {
		Contact existingContact = contactRepository.findById(id);
		return existingContact;
	}
}	
	
@Entity
class Contact {
	
	public Contact (){}
	
	public Contact(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	String firstName;
	String lastName;
}

@Component
@Lazy class ShortContact extends ResourceSupport{
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	String firstName;
	String lastName;
}
	@RepositoryRestResource
	@Lazy
	interface ContactRepository extends JpaRepository<Contact, Long> {
		Contact findById(@Param("id") Long id);
	}

	


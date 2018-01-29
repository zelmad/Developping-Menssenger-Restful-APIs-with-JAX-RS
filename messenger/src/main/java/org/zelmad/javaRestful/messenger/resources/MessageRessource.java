package org.zelmad.javaRestful.messenger.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.zelmad.javaRestful.messenger.model.Message;
import org.zelmad.javaRestful.messenger.resources.beans.MessageFilterBean;
import org.zelmad.javaRestful.messenger.service.MessageService;
import org.zelmad.javaRestful.messenger.resources.CommentResource;;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageRessource {

	MessageService messageService = new MessageService();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getJsonMessages(@BeanParam MessageFilterBean filterBean) {
		System.out.println("Json methode called");
		if(filterBean.getYear() > 0) {
			return messageService.getAllMessagesForYear(filterBean.getYear());
		}
		if(filterBean.getStart() >= 0 && filterBean.getSize() > 0) {
			return messageService.getAllMessagesPaginated(filterBean.getStart(), filterBean.getSize());
		}
		return messageService.getAllMessages();
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<Message> getXmlMessages(@BeanParam MessageFilterBean filterBean) {
		System.out.println("Xml methode called");
		if(filterBean.getYear() > 0) {
			return messageService.getAllMessagesForYear(filterBean.getYear());
		}
		if(filterBean.getStart() >= 0 && filterBean.getSize() > 0) {
			return messageService.getAllMessagesPaginated(filterBean.getStart(), filterBean.getSize());
		}
		return messageService.getAllMessages();
	}
	
	@POST
	public Response addMessage(Message message, @Context UriInfo uriInf){
		
		//System.out.print(uriInf.getAbsolutePathBuilder());
		Message newMessage = messageService.addMessage(message);
		String newUri = String.valueOf(newMessage.getId());
		URI uri = uriInf.getAbsolutePathBuilder().path(newUri).build();
		return Response.created(uri)
		        .entity(newMessage)
		        .build();
	}
	
	@PUT
	@Path("/{messageId}")
	public Message updateMessage(@PathParam("messageId") long id, Message message) {
		message.setId(id);
		return messageService.updateMessage(message);
	}
	
	@DELETE
	@Path("/{messageId}")
	public void deleteMessage(@PathParam("messageId") long id) {
		messageService.removeMessage(id);
	}
	
	@GET
	@Path("/{messageId}")
	public Message getMessage(@PathParam("messageId") long messageId, @Context UriInfo uriInf) {
		 Message message = messageService.getMessage(messageId); 
		  message.addLink(getUriForSelf(uriInf, message), "self");
		  message.addLink(getUriForProfile(uriInf, message), "profile");
		  message.addLink(getUriForComments(uriInf, message), "comments");
		  return message; 
	}
    
	private String getUriForComments(UriInfo uriInf, Message message) {
		URI uri = uriInf.getBaseUriBuilder()
				.path(MessageRessource.class)
				.path(MessageRessource.class, "getCommentResource")
				.path(CommentResource.class)
				//to change the {messageId} par l'Id de notre message
				.resolveTemplate("messageId", message.getId())
				.build();
		return uri.toString();
	}

	private String getUriForProfile(UriInfo uriInf, Message message) {
		URI uri  = uriInf.getBaseUriBuilder()
			       .path(ProfileRessource.class)
			       .path(message.getAuthor())
			       .build();
		return uri.toString();
	}

	private String getUriForSelf(UriInfo uriInf, Message message) {
		String uri  = uriInf.getBaseUriBuilder()
		       .path(MessageRessource.class)
		       .path(Long.toString(message.getId()))
		       .build()
		       .toString();
		return uri;
	}
	
	@Path("/{messageId}/comment")
	public CommentResource getCommentResource() {
		return new CommentResource();
	}
}

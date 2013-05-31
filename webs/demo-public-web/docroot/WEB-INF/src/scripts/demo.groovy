import com.liferay.scripting.executor.groovy.ScriptingContext
import com.liferay.scripting.executor.groovy.lib_62.Role
import com.liferay.scripting.executor.groovy.lib_62.User

def scriptingContext = new ScriptingContext();

def users = [
	["Bruno", "Admin", "Administrator", "bruno@liferay.com", "password",["Administrator"]],
	["Kendra", "User", "Administrator", "bruno@liferay.com", "password",["Users"]],
	["Richard", "Publisher", "Administrator", "bruno@liferay.com", "password",["Users","Publisher"]],
	["Michelle", "Editor", "Administrator", "bruno@liferay.com", "password",["Users","Editor"]]
];

for(user in users) {
	def userToCreate = new User(user[0],user[1],user[2],user[3],user[4]);
	userToCreate.create(scriptingContext);
	
	def rolesToAssign = user[5];
	for(roleToAssign in rolesToAssign){
		def role = Role.siteRole(roleToAssign, roleToAssign);
		def createdRole = role.create(scriptingContext);
		userToCreate.addRoles(scriptingContext, roleToAssign);
	}
}


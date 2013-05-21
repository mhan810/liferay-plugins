import com.liferay.scripting.executor.groovy.Organization
import com.liferay.scripting.executor.groovy.Role
import com.liferay.scripting.executor.groovy.ScriptingContext
import com.liferay.scripting.executor.groovy.Site


def scriptingContext = new ScriptingContext();

def organizationNames = ["Marketing", "HR", "Executives", "Legal"];

for (organizationName in organizationNames) {
	def organization = new Organization(organizationName);
	organization.create(scriptingContext);
}

def privateSiteNames = ["Marketing", "HR"];

for(siteName in privateSiteNames) {
	def site = Site.privateSite(siteName, "");
	site.create(scriptingContext);
}

def publicFacingSite = Site.openSite("Public-Facing", "");
publicFacingSite.create(scriptingContext);

def siteRoleNames = [
		"Web Content Creator",
		"Web Content Approver",
		"Press Release Creator",
		"Press Release Approver",
		"Message Board Moderator"
];

for (roleName in siteRoleNames) {
	def role = Role.siteRole(roleName, "");
	role.create(scriptingContext);
}
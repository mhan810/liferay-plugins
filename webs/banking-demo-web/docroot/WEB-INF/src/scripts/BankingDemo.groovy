import com.liferay.scripting.executor.groovy.Organization
import com.liferay.scripting.executor.groovy.Role
import com.liferay.scripting.executor.groovy.ScriptingContext
import com.liferay.scripting.executor.groovy.Site
import com.liferay.scripting.executor.groovy.User


def scriptingContext = new ScriptingContext();

def bankOrganizationName = "banking.liferay.com";

def organization = new Organization(bankOrganizationName);
organization.create(scriptingContext);

def commoditiesSite = Site.privateSite("Commodities", "");
def alternativeAssetsSite = Site.privateSite("Alternative Assets", "");
def fixedIncomeSite = Site.privateSite("Fixed Income", "");
def privateEquitySite = Site.privateSite("Private Equity", "");
def equitiesSite = Site.privateSite("Equities", "");

for(site in [commoditiesSite,alternativeAssetsSite,fixedIncomeSite,privateEquitySite,equitiesSite]){
	site.addOrganizations(scriptingContext, bankOrganizationName);
	site.create(scriptingContext);
}

def rolesToCreate = [
	["Financial Analyst","Creates content and posts relating to Finanacial Investments"],
	["Industry Analyst", "Creates content and posts relating to a Particular Industry sector (primarily in the Commodities area)."],
	["Sales Representative","Comments on existing content."]
]

for (newRole in rolesToCreate) {
	def role = Role.siteRole(newRole[0], newRole[1]);
	role.create(scriptingContext);
}

def usersToCreate = [
	["campbell","morris","campbell.morris@liferay.com","Sales Representative",["Commodities"]],
	["connie","modine","connie.modine@liferay.com","Lead Industry Analyst",["Commodities"]],
	["clark","mathis","clark.mathis@liferay.com","Financial Analyst",["Alternative Assets","Commodities"]],
	["irwin","norman","irwin.norman@liferay.com","Senior Sales Representative ",["Fixed Income"]],
	["irene","nance", "irene.nance@liferay.com","Lead Analyst",["Fixed Income"]],
	["reggie","franklin","reggie.franklin@liferay.com","Lead Analyst",["Equities"]],
	["renae","fortner", "renae.fortner@liferay.com","Senior Sales Representative",["Equities"]],
	["penny","hubert","penny.hubert@liferay.com","Senior Financial Analyst",["Alternative Assets"]],
	["ronald","esteves","ronald.esteves@liferay.com","Financial Analyst",["Alternative Assets"]],
	["max","young", "max.young@liferay.com","Sales Representative",["Commodities","Equities"]],
	["elton","worthington","elton.worthington@liferay.com","Financial Analyst",["Commodities","Private Equity"]],
	["herbert","nelson","herbert.nelson@liferay.com","Financial Analyst",["Commodities","Equities"]],
	["lauren","smith","lauren.smith@liferay.com","Financial Analyst",["Equities","Private Equity"]],
	["arthur","king","arthur.king@liferay.com","Senior Financial Analyst",["Alternative Assets"]]
]

for (userData in usersToCreate) {
	def user = new User(userData[0],userData[1],userData[3],userData[2],"password");
	user.create(scriptingContext);
	user.joinOrganizations(scriptingContext, bankOrganizationName);
	
	def userSites = userData[4];
	for(site in userSites) {
		user.joinSites(scriptingContext, site);
	}
}

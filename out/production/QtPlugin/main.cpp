#include "precompiled.h"

int main(int argc, char *argv[]) {
	QApplication app(argc, argv);
	app.setAttribute(Qt::AA_UseHighDpiPixmaps);
	app.setApplicationName("#APP_NAME#");
	app.setOrganizationDomain("#ORG_DOMAIN#");
	app.setOrganizationName("#ORG_NAME#");

    QIcon appIcon;
    appIcon.addFile(":/Icons/AppIcon32");
    appIcon.addFile(":/Icons/AppIcon128");
    app.setWindowIcon(appIcon);


    // append ...
	return app.exec();
}

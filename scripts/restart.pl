use strict;
use Data::Dumper;

my @result = `ps x | grep maven`;
@result = grep (!m/grep/, @result);

if(@result){
   my @values = split(" ", $result[0]);
   my $kill_command = "kill -9 $values[0]";
   `$kill_command`;
   print "tomcat stopped.\n"
}
else{
   print "tomcat not running.\n";
}
my $path = "../tool";
chdir($path) or die "Cant chdir to $path $!";
print "Starting tomcat...\nBe patient and enjoy the meaningless output :-)\n";
exec("mvn tomcat6:run &");


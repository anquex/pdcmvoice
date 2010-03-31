clear all;
close all;
clc;

hold all;
grid on;

%fh = figure(1); % returns the handle to the figure object
%set(fh, 'color', 'white'); % sets the color to white

ColorOrder=ColorSpiral(7,2,0);
set(gca,'ColorOrder',ColorOrder(2:end-1,:));

axis([1 4500.001 0 1])
n=1024;
p=1/n;

x=[1:4500];
plot(x,prob_coll(p,x));
plot(x,prob_succ(p,x));
plot(x,prob_idle(p,x));

legend('collision','success','idle','Location','East');
%hl = legend;
%set(hl, 'Interpreter', 'Latex');
%set(hl, 'FontSize', 12);
hx=xlabel('n','Interpreter','Latex');
hy=ylabel('Event Probability','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);

saveas(gca,'draw_coll_idle_succ_fixed_p', 'epsc');


%set(gca,'XScale','log');
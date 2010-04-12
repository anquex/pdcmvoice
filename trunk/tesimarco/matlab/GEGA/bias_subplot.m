% bias plot

close all;
clear all;
hold all;
clc;

load('GMLE-bias-T10');
subplot(2,2,1)
sxlim=find(batchsizes>=20,'1');
h10=plot(batchsizes(sxlim:end),bias(sxlim:end));
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE
grid on;
set(gca,'YLim',[1.07 1.0725])
set(gca,'XLim',[20 2000])
legend('$T=10$','Location','South');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);
hx=xlabel('$n$','Interpreter','Latex');
hy=ylabel('$E[\hat{n}|n]/n$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);



load(['GMLE-bias-T20']);
subplot(2,2,2)
sxlim=find(batchsizes>=20,'1');
h20=plot(batchsizes(sxlim:end),bias(sxlim:end));
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE
legend('$T=20$','Location','South');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);
grid on;
set(gca,'YLim',[1.0405 1.0435])
set(gca,'XLim',[20 2000])
hx=xlabel('$n$','Interpreter','Latex');
hy=ylabel('$E[\hat{n}|n]/n$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);



load(['GMLE-bias-T30']);
subplot(2,2,3)
sxlim=find(batchsizes>=20,'1');
h30=plot(batchsizes(sxlim:end),bias(sxlim:end));
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE
legend('$T=30$','Location','North');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);
hx=xlabel('$n$','Interpreter','Latex');
hy=ylabel('$E[\hat{n}|n]/n$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);
set(gca,'YLim',[1.0295 1.0307])
grid on;
set(gca,'XLim',[20 2000])

fh = figure(1); % returns the handle to the figure object
set(fh,'PaperSize',[20.984 29.6774])
set(fh,'Position',[440 175 773 603])
saveas(fh, 'GEGA-bias-varying-T', 'epsc');
return;



set(fh, 'color', 'white'); % sets the color to white

set(gca, 'Box', 'on' ); % here gca means get current axis
grid on;

set(h10, 'LineStyle', '-.', 'LineWidth', 1.0, 'Color', 'Black');
set(h20, 'LineStyle', '--', 'LineWidth', 1.0, 'Color', 'Black');
set(h30, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');

%set(gca,'YTickLabel',)
set(gca,'YLim',[1.029 1.073])
set(gca,'XLim',[20 2000])
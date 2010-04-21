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
%set(gca,'XScale','log');
set(gca,'YLim',[1.07 1.0725])
set(gca,'XLim',[20 2000])
set(gca,'XTick',[100 500 1000 1500 2000])
set(gca,'XTickLabel',[100 500 1000 1500 2000])
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
set(gca,'XTick',[100 500 1000 1500 2000])
set(gca,'XTickLabel',[100 500 1000 1500 2000])
%set(gca,'XScale','log');
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
%set(gca,'XScale','log');
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE
set(gca,'XTick',[100 500 1000 1500 2000])
set(gca,'XTickLabel',[100 500 1000 1500 2000])
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
set(fh, 'color', 'white'); % sets the color to white

set(gca, 'Box', 'on' ); % here gca means get current axis
grid on;

clear all;
subplot(2,2,4);
hold all;
clc;

load('GMLE-bias-T10');
hh10=plot(batchsizes,bias);
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

load(['GMLE-bias-T20']);
hh20=plot(batchsizes,bias);
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

load(['GMLE-bias-T30']);
hh30=plot(batchsizes,bias);
%clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

legend('$T=10$','$T=20$','$T=30$','Location','Best');
hhl = legend;
set(hhl, 'Interpreter', 'Latex');
set(hhl, 'FontSize', 11);

hx=xlabel('$n$','Interpreter','Latex');
hy=ylabel('$E[\hat{n}|n]/n$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);



fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(gca, 'Box', 'on' ); % here gca means get current axis
grid on;

set(hh10, 'LineStyle', '-.', 'LineWidth', 1.3, 'Color', 'Black');
set(hh20, 'LineStyle', '--', 'LineWidth', 1.0, 'Color', 'Black');
set(hh30, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');

%set(gca,'YTickLabel',)
set(gca,'YLim',[1.029 1.075])
set(gca,'XLim',[5 2000])
set(gca,'XScale','log');


%set(fh,'PaperSize',[20.984 29.6774])
%set(fh,'Position',[187 142 952 642])
%set(gca,'Position',[0.60278 0.118268 0.30222 0.332895]);
%saveas('GEGA-bias-varying-T', 'epsc');